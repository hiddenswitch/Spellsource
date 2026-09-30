import React from 'react'
import { Text } from '@react-three/drei'

/**
 * Rotation to lay flat on the XZ plane, readable from the camera at (0, 9, -4.5).
 * -PI/2 on X lays it flat; PI on Z flips it right-side-up for the camera's viewpoint.
 */
const FLAT_ROTATION: [number, number, number] = [-Math.PI / 2, 0, Math.PI]

interface StatBadge3DProps {
  value: number
  color: string
  position: [number, number, number]
  /** Text color override for buff/debuff */
  textColor?: string
  radius?: number
}

/**
 * A 3D stat badge: colored circle with a number on top.
 * Placed as a child of the entity mesh so it inherits rotation/position.
 */
export const StatBadge3D: React.FC<StatBadge3DProps> = ({
  value,
  color,
  position,
  textColor = '#ffffff',
  radius = 0.1,
}) => {
  return (
    <group position={position}>
      {/* Background circle */}
      <mesh rotation={FLAT_ROTATION} position={[0, 0.001, 0]}>
        <circleGeometry args={[radius, 16]} />
        <meshBasicMaterial color={color} depthTest={false} />
      </mesh>
      {/* Number text */}
      <Text
        position={[0, 0.002, 0]}
        rotation={FLAT_ROTATION}
        fontSize={radius * 1.3}
        color={textColor}
        anchorX="center"
        anchorY="middle"
        fontWeight={700}
        outlineWidth={radius * 0.08}
        outlineColor="#000000"
        depthOffset={-1}
      >
        {String(value)}
      </Text>
    </group>
  )
}

interface Label3DProps {
  text: string
  position: [number, number, number]
  fontSize?: number
  maxWidth?: number
  color?: string
  /** Width/height of a dark background panel behind the text. Omit for no background. */
  bgWidth?: number
  bgHeight?: number
}

/**
 * A 3D text label with optional dark background for legibility.
 * Placed as child of entity mesh.
 */
export const Label3D: React.FC<Label3DProps> = ({
  text,
  position,
  fontSize = 0.07,
  maxWidth = 0.6,
  color = '#ffffff',
  bgWidth,
  bgHeight,
}) => {
  return (
    <group position={position}>
      {/* Optional dark background */}
      {bgWidth != null && bgHeight != null && (
        <mesh rotation={FLAT_ROTATION} position={[0, -0.0005, 0]}>
          <planeGeometry args={[bgWidth, bgHeight]} />
          <meshBasicMaterial color="#000000" transparent opacity={0.55} depthTest={false} />
        </mesh>
      )}
      <Text
        position={[0, 0.0005, 0]}
        rotation={FLAT_ROTATION}
        fontSize={fontSize}
        color={color}
        anchorX="center"
        anchorY="middle"
        maxWidth={maxWidth}
        textAlign="center"
        outlineWidth={fontSize * 0.12}
        outlineColor="#000000"
        depthOffset={-1}
        whiteSpace="nowrap"
      >
        {text}
      </Text>
    </group>
  )
}
