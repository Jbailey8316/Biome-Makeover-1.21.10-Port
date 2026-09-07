# Stage 14A — Mythas Enhancement / Config Framework

Status: implemented foundation; Prism validation required. This stage adds
configuration infrastructure only. It does not implement Trial Wing,
Decayed Shield, dynamic Lightning Bugs, or cosmetic behavior.

## Boundary

Mythas is an enhancement layer separate from released Biome Makeover parity.
With all Mythas enhancements disabled, behavior is intended to match the
frozen released-parity Biome Makeover baseline. No existing released
registration, resource, Mansion template, or Adjudicator behavior is changed
by this framework.

## Configuration architecture

The repository had no existing BM configuration serializer, config file, or
Cloth/Mod Menu dependency. Stage 14A therefore uses a small independent JSON
file at:

`config/biomemakeover-mythas.json`

The file is located through Fabric Loader's config directory and parsed with
the Gson library already supplied by Minecraft. Unknown JSON properties are
preserved. Missing keys are added with safe defaults. A malformed document is
left intact for inspection and the in-memory state becomes all-off; no
enhancement defaults on after an error.

The server/integrated-server loads this configuration during mod
initialization. It is therefore server-authoritative for future gameplay.
There is no client config screen in this stage: the project has no existing
config-screen integration, and adding a large UI dependency solely for this
foundation is not justified. A future UI must clearly label the section
“MYTHAS ENHANCEMENTS” and “Not part of original Biome Makeover.”

## Keys and defaults

All values default to `false`:

```json
{
  "mythas": {
    "enabled": false,
    "mansion_trial_wing": false,
    "decayed_shield_upgrade": false,
    "dynamic_lightning_bugs": false,
    "cosmetic_polish": false
  }
}
```

The master setting is `mythas.enabled`. The initial feature settings are:

- `mythas.mansion_trial_wing.enabled`
- `mythas.decayed_shield_upgrade.enabled`
- `mythas.dynamic_lightning_bugs.enabled`
- `mythas.cosmetic_polish.enabled`

The JSON uses the final path component as the property name inside the
`mythas` object. No Owl expansion or Toad revival toggle is included.

## Effective-state API

Future code must use `party.lemons.biomemakeover.config.MythasConfig` rather
than parsing JSON directly. The public accessors are:

- `isEnabled()`
- `isMansionTrialWingEnabled()`
- `isDecayedShieldUpgradeEnabled()`
- `isDynamicLightningBugsEnabled()`
- `isCosmeticPolishEnabled()`

Every feature accessor returns `master && child`. Child preferences are not
mutated when the master is off, so turning the master back on restores the
stored child choices.

## Future behavior contracts

| Feature | Kind | Intended toggle behavior |
| --- | --- | --- |
| Mansion Trial Wing | generation/activation | applies to newly generated Mansions; an existing generated wing is not retro-deleted |
| Decayed Shield Upgrade | gameplay | immediate effective-state check where practical |
| Dynamic Lightning Bugs | runtime/client presentation | immediate effective-state check where practical |
| Cosmetic Polish | presentation/runtime | immediate effective-state check where practical |

All future IDs remain registered regardless of toggle state. Toggles control
generation, activation, behavior, accessibility, or optional presentation;
they never conditionally remove registry entries. This preserves save and
network registry stability.

## Validation and runtime contract

`validation/Invoke-Stage14AMythasConfigValidation.ps1` asserts safe-off
defaults, master/child effective gates, absence of Stage 14B gameplay, tag
invariants, and the frozen Mansion inventory. The existing Stage 13 parity
suite remains the regression boundary.

Prism must verify first-launch file creation, all-off defaults, normal
released behavior, persistence across restart, master-off overriding a child
that is on, master-plus-child effective state, and safe malformed-config
recovery. No visible Mythas gameplay change is expected from Stage 14A.
