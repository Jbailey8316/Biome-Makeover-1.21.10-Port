# Stage 7 Runtime Remediation 1 — Owl Breeding and Black Thistle

## Owl breeding crash

### Trigger and call chain

Feeding two tame adult Owls entered normal love/breeding processing. When the
offspring was assigned its baby age, vanilla refreshed its dimensions:

`age/baby state -> refreshDimensions -> LivingEntity.getDimensions(Pose)`
`-> OwlEntity.getDefaultDimensions(Pose) -> super.getDimensions(Pose)`
`-> LivingEntity.getDimensions(Pose) -> OwlEntity.getDefaultDimensions(Pose)`

The last two calls repeated until `StackOverflowError`. The reported Stage 7
line 244 was the `super.getDimensions(pose)` call.

### Root cause and fix

Final BM 1.20.1 overrode the public, non-final `getDimensions(Pose)` and called
its superclass implementation for the standing state. In Minecraft 1.21.10,
`LivingEntity.getDimensions(Pose)` is final and delegates dynamic behavior to
the protected `getDefaultDimensions(Pose)` hook. The initial port moved the
override but mechanically retained the old superclass call, creating virtual
redispatch into itself.

The standing branch now calls `super.getDefaultDimensions(pose)`; the flying
branch still returns the historical `0.7 × 1.4` dimensions. No recursion guard,
custom breeding, food restriction or breeding bypass was added.

The food, interaction, tame, heal, love-mode and offspring paths were audited.
They retain the final contract: modern `minecraft:wolf_food` meat semantics,
1-in-3 taming, nutrition healing, ordinary breeding, baby Owl creation and
owner/tame inheritance. No sibling override contains the same final-wrapper to
dynamic-hook recursion pattern.

## Black Thistle

Final 1.20.1 applies Weakness for 110 ticks at amplifier 0 when a vulnerable
LivingEntity is inside the upper half. Rootlings, Owls and Bees are excluded.
The server reapplies the effect while contact continues, naturally refreshing
its duration. The final code does not use berry-bush slowdown or contact damage.

The port had a non-overriding four-argument `entityInside`-like method in the
wrong argument order. Minecraft 1.21.10 calls the six-argument protected
`entityInside(BlockState, Level, BlockPos, Entity, InsideBlockEffectApplier,
boolean)` hook, so the old method was never dispatched. It also contained
nonhistorical Fox/Bee exclusions, movement slowdown and damage.

The correct 1.21.10 callback now reproduces the released upper-half Weakness
contract and Wither-damage invulnerability check. Rootling exclusion is kept by
canonical entity ID without registering or activating Stage 8 Rootlings.
Generation, models, textures, two-block placement and flower mixture are
unchanged.

## Validation and runtime status

The validator now rejects dynamic-dimension hooks that call the final wrapper
and asserts Black Thistle's modern callback, upper-half Weakness values and
historical Owl/Bee exclusions. Static checks cannot prove breeding completion,
baby ownership, effect refresh or runtime dimension transitions; those remain
the focused Prism gate.
