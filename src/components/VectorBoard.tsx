import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { Arrow } from '../game/Arrow';
import { BoardState } from '../game/BoardState';
import { Direction, DirectionType } from '../game/Direction';
import { MoveValidator, PathCell } from '../game/MoveValidator';
import { SoundManager } from '../audio/SoundManager';
import { HapticManager } from '../haptics/HapticManager';

interface Particle {
  id: number;
  x: number;
  y: number;
  vx: number;
  vy: number;
  color: string;
  alpha: number;
  size: number;
}

interface FlyingArrow {
  arrow: Arrow;
  startRow: number;
  startCol: number;
  progress: number; // 0 to 1
  opacity: number;
}

interface VectorBoardProps {
  board: BoardState;
  onMoveSuccess: (arrow: Arrow) => void;
  onMoveInvalid: (arrow: Arrow, blocker: Arrow | null) => void;
  activeHintArrowId?: string | null;
  disabled?: boolean;
  reducedMotion?: boolean;
}

export const VectorBoard: React.FC<VectorBoardProps> = ({
  board,
  onMoveSuccess,
  onMoveInvalid,
  activeHintArrowId,
  disabled = false,
  reducedMotion = false
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  // Animation states
  const [flyingArrow, setFlyingArrow] = useState<FlyingArrow | null>(null);
  const [shakingArrowId, setShakingArrowId] = useState<string | null>(null);
  const [shakeAxis, setShakeAxis] = useState<'x' | 'y'>('x');
  const [obstructionInfo, setObstructionInfo] = useState<{
    arrowId: string;
    pathCells: PathCell[];
    blockerId: string | null;
  } | null>(null);

  // Path preview state (press and hold)
  const [previewArrow, setPreviewArrow] = useState<Arrow | null>(null);
  const holdTimerRef = useRef<NodeJS.Timeout | null>(null);
  const touchStartPosRef = useRef<{ x: number; y: number } | null>(null);
  const isInputLockedRef = useRef(false);

  // Flying particles array ref
  const particlesRef = useRef<Particle[]>([]);
  const nextParticleIdRef = useRef(1);

  // Legal moves list memoized for lane lighting
  const legalMoves = useMemo(() => {
    return MoveValidator.getLegalMoves(board);
  }, [board]);

  const legalArrowIds = useMemo(() => {
    return new Set(legalMoves.map(a => a.id));
  }, [legalMoves]);

  // Compute preview trajectory for the previewed arrow or hint arrow
  const previewTrajectory = useMemo(() => {
    const target = previewArrow || (activeHintArrowId ? board.getArrowById(activeHintArrowId) : null);
    if (!target) return null;
    return MoveValidator.getPreviewCells(board, target);
  }, [previewArrow, activeHintArrowId, board]);

  // Particle emission helper
  const emitTrailParticles = (x: number, y: number, dir: DirectionType) => {
    const { dr, dc } = Direction.getVector(dir);
    const count = 4;
    for (let i = 0; i < count; i++) {
      const angleOffset = (Math.random() - 0.5) * 0.8;
      const speed = 1.5 + Math.random() * 2.5;
      const vx = -dc * speed + (Math.random() - 0.5) * 1.5;
      const vy = -dr * speed + (Math.random() - 0.5) * 1.5;

      particlesRef.current.push({
        id: nextParticleIdRef.current++,
        x: x + (Math.random() - 0.5) * 12,
        y: y + (Math.random() - 0.5) * 12,
        vx,
        vy,
        color: Math.random() > 0.3 ? '#00F0FF' : '#A78BFA',
        alpha: 0.9,
        size: 2.5 + Math.random() * 2.5
      });
    }
  };

  // Particle emission for blocked sparks
  const emitBlockedSparks = (x: number, y: number) => {
    const count = 8;
    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = 1.0 + Math.random() * 2.0;
      particlesRef.current.push({
        id: nextParticleIdRef.current++,
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        color: Math.random() > 0.5 ? '#F87171' : '#FBBF24',
        alpha: 1.0,
        size: 2.5 + Math.random() * 2
      });
    }
  };

  // Canvas render loop for high-performance particle & flight rendering
  useEffect(() => {
    let animId: number;

    const render = () => {
      const canvas = canvasRef.current;
      if (!canvas) return;
      const ctx = canvas.getContext('2d');
      if (!ctx) return;

      // Match canvas dimensions to client dimensions
      const rect = canvas.getBoundingClientRect();
      const dpr = window.devicePixelRatio || 1;
      if (canvas.width !== rect.width * dpr || canvas.height !== rect.height * dpr) {
        canvas.width = rect.width * dpr;
        canvas.height = rect.height * dpr;
      }
      ctx.save();
      ctx.scale(dpr, dpr);
      ctx.clearRect(0, 0, rect.width, rect.height);

      // Render & update particles
      const particles = particlesRef.current;
      for (let i = particles.length - 1; i >= 0; i--) {
        const p = particles[i];
        p.x += p.vx;
        p.y += p.vy;
        p.alpha -= 0.035;
        p.size *= 0.96;

        if (p.alpha <= 0.05 || p.size <= 0.5) {
          particles.splice(i, 1);
          continue;
        }

        ctx.save();
        ctx.globalAlpha = p.alpha;
        ctx.fillStyle = p.color;
        ctx.shadowColor = p.color;
        ctx.shadowBlur = 6;
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
      }

      ctx.restore();
      animId = requestAnimationFrame(render);
    };

    animId = requestAnimationFrame(render);
    return () => cancelAnimationFrame(animId);
  }, []);

  // Handle arrow click/tap
  const handleArrowTap = useCallback(
    (arrow: Arrow) => {
      if (disabled || isInputLockedRef.current) return;

      const isLegal = MoveValidator.isMoveLegal(board, arrow);

      if (isLegal) {
        // Legal move: launch!
        isInputLockedRef.current = true;
        setPreviewArrow(null);
        setObstructionInfo(null);

        // Sound & Haptics
        SoundManager.playLaunch();
        HapticManager.lightTap();

        if (reducedMotion) {
          // Instant removal if reduced motion
          isInputLockedRef.current = false;
          onMoveSuccess(arrow);
          return;
        }

        // Setup flight animation
        setFlyingArrow({
          arrow,
          startRow: arrow.row,
          startCol: arrow.col,
          progress: 0,
          opacity: 1
        });

        // Animate flight across 340ms
        const startTime = performance.now();
        const duration = 340;

        const animateFlight = (now: number) => {
          const elapsed = now - startTime;
          const rawProgress = Math.min(elapsed / duration, 1);
          // Ease-in quad/cubic acceleration: shoots off screen!
          const easeProgress = Math.pow(rawProgress, 1.8);

          // Calculate current pixel position for trail particles
          if (containerRef.current) {
            const containerRect = containerRef.current.getBoundingClientRect();
            const cellW = containerRect.width / board.cols;
            const cellH = containerRect.height / board.rows;
            const startX = (arrow.col + 0.5) * cellW;
            const startY = (arrow.row + 0.5) * cellH;

            const { dr, dc } = Direction.getVector(arrow.direction);
            const dist = Math.max(board.rows, board.cols) * 1.5;
            const currentX = startX + dc * cellW * dist * easeProgress;
            const currentY = startY + dr * cellH * dist * easeProgress;

            emitTrailParticles(currentX, currentY, arrow.direction);
          }

          setFlyingArrow({
            arrow,
            startRow: arrow.row,
            startCol: arrow.col,
            progress: easeProgress,
            opacity: 1 - Math.max(0, (rawProgress - 0.5) * 2)
          });

          if (rawProgress < 1) {
            requestAnimationFrame(animateFlight);
          } else {
            setFlyingArrow(null);
            isInputLockedRef.current = false;
            onMoveSuccess(arrow);
          }
        };

        requestAnimationFrame(animateFlight);
      } else {
        // Blocked move: subtle obstruction feedback
        const blocker = MoveValidator.getFirstBlockingArrow(board, arrow);
        const { cells } = MoveValidator.getPreviewCells(board, arrow);

        SoundManager.playBlocked();
        HapticManager.blocked();

        const isHorizontal = arrow.direction === 'LEFT' || arrow.direction === 'RIGHT';
        setShakeAxis(isHorizontal ? 'x' : 'y');
        setShakingArrowId(arrow.id);
        setObstructionInfo({
          arrowId: arrow.id,
          pathCells: cells,
          blockerId: blocker ? blocker.id : null
        });

        // Spark particles at arrow position
        if (containerRef.current) {
          const containerRect = containerRef.current.getBoundingClientRect();
          const cellW = containerRect.width / board.cols;
          const cellH = containerRect.height / board.rows;
          const cx = (arrow.col + 0.5) * cellW;
          const cy = (arrow.row + 0.5) * cellH;
          emitBlockedSparks(cx, cy);
        }

        setTimeout(() => {
          setShakingArrowId(null);
        }, 320);

        setTimeout(() => {
          setObstructionInfo(null);
        }, 650);

        onMoveInvalid(arrow, blocker);
      }
    },
    [board, disabled, onMoveSuccess, onMoveInvalid, reducedMotion]
  );

  // Press & hold handlers for Path Preview
  const handlePointerDown = (arrow: Arrow, e: React.PointerEvent) => {
    if (disabled || isInputLockedRef.current) return;
    touchStartPosRef.current = { x: e.clientX, y: e.clientY };

    // Trigger preview if held for 180ms
    if (holdTimerRef.current) clearTimeout(holdTimerRef.current);
    holdTimerRef.current = setTimeout(() => {
      setPreviewArrow(arrow);
      HapticManager.lightTap();
    }, 180);
  };

  const handlePointerUp = (arrow: Arrow, e: React.PointerEvent) => {
    if (holdTimerRef.current) {
      clearTimeout(holdTimerRef.current);
      holdTimerRef.current = null;
    }

    // Check if it was a quick tap or release
    if (touchStartPosRef.current) {
      const dx = Math.abs(e.clientX - touchStartPosRef.current.x);
      const dy = Math.abs(e.clientY - touchStartPosRef.current.y);
      touchStartPosRef.current = null;

      // If dragged significantly, don't tap
      if (dx < 15 && dy < 15) {
        handleArrowTap(arrow);
      }
    }
    setPreviewArrow(null);
  };

  const handlePointerCancel = () => {
    if (holdTimerRef.current) {
      clearTimeout(holdTimerRef.current);
      holdTimerRef.current = null;
    }
    touchStartPosRef.current = null;
    setPreviewArrow(null);
  };

  // Render SVG Arrow Graphic with sleek futuristic geometric details
  const renderArrowSvg = (direction: DirectionType, isLegal: boolean, isHint: boolean, isObstruction: boolean) => {
    const angle = Direction.getVector(direction).angleDeg;

    let strokeColor = '#00F0FF';
    let glowColor = 'rgba(0, 240, 255, 0.4)';
    let fillColor = 'rgba(0, 240, 255, 0.25)';

    if (isObstruction) {
      strokeColor = '#EF4444';
      glowColor = 'rgba(239, 68, 68, 0.5)';
      fillColor = 'rgba(239, 68, 68, 0.3)';
    } else if (isHint) {
      strokeColor = '#10B981';
      glowColor = 'rgba(16, 185, 129, 0.6)';
      fillColor = 'rgba(16, 185, 129, 0.35)';
    } else if (!isLegal) {
      // Subdued cyan-slate when blocked, so player can still read direction clearly
      strokeColor = '#60A5FA';
      glowColor = 'rgba(96, 165, 250, 0.15)';
      fillColor = 'rgba(96, 165, 250, 0.12)';
    }

    return (
      <svg
        viewBox="0 0 44 44"
        className="w-full h-full p-1.5 transition-transform duration-200"
        style={{
          transform: `rotate(${angle}deg)`,
          filter: `drop-shadow(0 0 6px ${glowColor})`
        }}
      >
        <defs>
          <linearGradient id={`grad_${direction}_${isLegal ? '1' : '0'}`} x1="0" y1="1" x2="0" y2="0">
            <stop offset="0%" stopColor={strokeColor} stopOpacity="0.4" />
            <stop offset="100%" stopColor={strokeColor} stopOpacity="1.0" />
          </linearGradient>
        </defs>

        {/* Back glow disc */}
        <circle cx="22" cy="22" r="16" fill={fillColor} />

        {/* Arrow shaft */}
        <line
          x1="22"
          y1="34"
          x2="22"
          y2="14"
          stroke={`url(#grad_${direction}_${isLegal ? '1' : '0'})`}
          strokeWidth="3.6"
          strokeLinecap="round"
        />

        {/* Dynamic Chevron / Arrowhead */}
        <path
          d="M 12 19 L 22 7 L 32 19"
          fill="none"
          stroke={strokeColor}
          strokeWidth="3.8"
          strokeLinecap="round"
          strokeLinejoin="round"
        />

        {/* Energy core near base */}
        <circle
          cx="22"
          cy="31"
          r="2.2"
          fill={isHint ? '#10B981' : isObstruction ? '#EF4444' : '#00F0FF'}
          className={isLegal ? 'animate-pulse' : ''}
        />
      </svg>
    );
  };

  return (
    <div className="relative w-full max-w-[440px] aspect-square select-none touch-none p-3 mx-auto">
      {/* Precision Puzzle Board Device Frame */}
      <div
        ref={containerRef}
        className="relative w-full h-full rounded-3xl p-3 bg-gradient-to-b from-[#0E1729] via-[#0A111F] to-[#060A13] border border-cyan-500/20 shadow-[0_12px_40px_rgba(0,0,0,0.8),inset_0_1px_1px_rgba(255,255,255,0.1),0_0_20px_rgba(0,240,255,0.06)] flex flex-col justify-between overflow-hidden"
      >
        {/* Subtle grid atmospheric lines */}
        <div
          className="absolute inset-0 opacity-10 pointer-events-none"
          style={{
            backgroundImage: `radial-gradient(circle at 50% 50%, rgba(0, 240, 255, 0.25) 0%, transparent 70%)`
          }}
        />

        {/* Canvas overlay for particles & flight glow */}
        <canvas
          ref={canvasRef}
          className="absolute inset-0 w-full h-full pointer-events-none z-30"
        />

        {/* Grid Cells */}
        <div
          className="relative z-10 grid w-full h-full gap-2"
          style={{
            gridTemplateRows: `repeat(${board.rows}, minmax(0, 1fr))`,
            gridTemplateColumns: `repeat(${board.cols}, minmax(0, 1fr))`
          }}
        >
          {Array.from({ length: board.rows }).map((_, r) =>
            Array.from({ length: board.cols }).map((_, c) => {
              const arrow = board.getArrowAt(r, c);
              const isOccupied = arrow !== null;
              const isLegal = arrow ? legalArrowIds.has(arrow.id) : false;
              const isHint = arrow ? arrow.id === activeHintArrowId : false;
              const isShaking = arrow ? arrow.id === shakingArrowId : false;
              const isBlockedObstruction = arrow ? obstructionInfo?.arrowId === arrow.id : false;
              const isTargetBlocker = arrow ? obstructionInfo?.blockerId === arrow.id : false;

              // Check if this cell is part of a legal lane illumination or preview path
              const isInPreviewPath = previewTrajectory?.cells.some(
                cell => cell.row === r && cell.col === c
              );
              const isObstructionCell = obstructionInfo?.pathCells.some(
                cell => cell.row === r && cell.col === c
              );

              return (
                <div
                  key={`${r}-${c}`}
                  className={`relative rounded-xl flex items-center justify-center transition-all duration-200 ${
                    isOccupied
                      ? 'bg-[#121D33] border border-cyan-500/25 shadow-[inset_0_1px_1px_rgba(255,255,255,0.12),0_4px_12px_rgba(0,0,0,0.5)]'
                      : 'bg-[#0B1220]/60 border border-slate-800/40 shadow-inner'
                  } ${isInPreviewPath ? (previewTrajectory?.blocked ? 'bg-amber-950/40 border-amber-500/40' : 'bg-cyan-950/40 border-cyan-400/40') : ''} ${
                    isObstructionCell ? 'bg-red-950/40 border-red-500/50' : ''
                  }`}
                >
                  {/* Subtle lane illumination if legal and idle */}
                  {isOccupied && isLegal && !isShaking && (
                    <div className="absolute inset-0 rounded-xl bg-cyan-400/5 ring-1 ring-cyan-400/30 animate-pulse pointer-events-none" />
                  )}

                  {/* Active Hint Glow */}
                  {isHint && (
                    <div className="absolute -inset-1 rounded-2xl bg-emerald-500/20 ring-2 ring-emerald-400 shadow-[0_0_16px_rgba(16,185,129,0.8)] animate-pulse pointer-events-none z-20" />
                  )}

                  {/* Blocker Flash */}
                  {isTargetBlocker && (
                    <div className="absolute -inset-1 rounded-2xl bg-red-500/20 ring-2 ring-red-400 shadow-[0_0_14px_rgba(239,68,68,0.7)] animate-bounce pointer-events-none z-20" />
                  )}

                  {/* Arrow Tile Button */}
                  {arrow && (
                    <button
                      type="button"
                      aria-label={`Arrow pointing ${arrow.direction} at row ${r + 1}, column ${c + 1}${isLegal ? ', unblocked' : ', blocked'}`}
                      onPointerDown={e => handlePointerDown(arrow, e)}
                      onPointerUp={e => handlePointerUp(arrow, e)}
                      onPointerCancel={handlePointerCancel}
                      className={`relative z-10 w-full h-full rounded-xl flex items-center justify-center cursor-pointer active:scale-90 transition-transform duration-100 outline-none focus-visible:ring-2 focus-visible:ring-cyan-300 ${
                        isShaking
                          ? shakeAxis === 'x'
                            ? 'translate-x-1.5 duration-75 text-red-400'
                            : 'translate-y-1.5 duration-75 text-red-400'
                          : ''
                      }`}
                    >
                      {renderArrowSvg(arrow.direction, isLegal, isHint, isBlockedObstruction)}
                    </button>
                  )}

                  {/* Empty cell indicator dot */}
                  {!arrow && (
                    <div className="w-1.5 h-1.5 rounded-full bg-slate-700/30" />
                  )}
                </div>
              );
            })
          )}
        </div>

        {/* Flying Arrow Exit Layer */}
        {flyingArrow && containerRef.current && (
          <div
            className="absolute pointer-events-none z-40 transition-opacity"
            style={{
              width: `${100 / board.cols}%`,
              height: `${100 / board.rows}%`,
              left: `${(flyingArrow.startCol / board.cols) * 100}%`,
              top: `${(flyingArrow.startRow / board.rows) * 100}%`,
              transform: `translate(${
                Direction.getVector(flyingArrow.arrow.direction).dc *
                flyingArrow.progress *
                board.cols *
                140
              }%, ${
                Direction.getVector(flyingArrow.arrow.direction).dr *
                flyingArrow.progress *
                board.rows *
                140
              }%) scale(${1 + flyingArrow.progress * 0.2})`,
              opacity: flyingArrow.opacity
            }}
          >
            <div className="w-full h-full p-1 drop-shadow-[0_0_14px_#00F0FF]">
              {renderArrowSvg(flyingArrow.arrow.direction, true, false, false)}
            </div>
          </div>
        )}
      </div>

      {/* Path preview indicator legend */}
      {previewArrow && (
        <div className="absolute -bottom-8 left-0 right-0 flex items-center justify-center text-xs tracking-wider font-mono text-cyan-300 animate-pulse">
          <span>
            {previewTrajectory?.blocked
              ? `BLOCKED BY OBSTACLE`
              : `UNBLOCKED ESCAPE PATH`}
          </span>
        </div>
      )}
    </div>
  );
};
