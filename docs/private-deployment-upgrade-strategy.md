# Private Deployment Upgrade Strategy

## Scope
- Commercial model: private deployment is `buyout`.
- Support policy: 12 months technical support included.
- Upgrade policy: upgrades are free during support period.
- Renewal policy: paid support renewal extends upgrade entitlement.

## Authorization Model
- Entry point: hospital inputs an activation code (`IMLD_ACTIVATION_CODE`).
- Runtime authorization: system validates a signed `license.json` using vendor public key.
- Upgrade authorization: system validates a signed `release-manifest.json` and checks support window.

## Signed License Envelope
- File path: `imld.licensing.private-edition.license-file-path`.
- Structure:
```json
{
  "payload": {
    "licenseId": "LIC-2026-0001",
    "hospitalId": "HOSP-001",
    "deploymentMode": "private",
    "supportStartDate": "2026-01-01",
    "supportEndDate": "2026-12-31",
    "activationCodeHash": "SHA256_HEX",
    "machineFingerprintHash": "OPTIONAL_SHA256_HEX",
    "features": ["core", "followup"],
    "scenarios": ["followup.push", "vip.subscription"]
  },
  "signature": "BASE64_RSA_SHA256_SIGNATURE"
}
```

## Signed Release Manifest Envelope
- File path: `imld.upgrade.manifest-file-path`.
- Structure:
```json
{
  "payload": {
    "version": "1.4.0",
    "releaseDate": "2026-10-01",
    "securityPatch": false,
    "channel": "lts"
  },
  "signature": "BASE64_RSA_SHA256_SIGNATURE"
}
```

## Runtime Decision Rules
- Private mode startup fails if:
  - license/public key file missing,
  - license signature invalid,
  - activation code hash mismatch,
  - machine fingerprint mismatch (when enabled),
  - deployment mode in license does not match runtime mode.
- Upgrade check fails if:
  - release manifest signature invalid,
  - `releaseDate > supportEndDate` and it is not an allowed security patch.

## Operational Flow
1. Customer deploys image with `private` profile.
2. Customer provides activation code and signed license file.
3. Startup validator verifies entitlement.
4. On upgrade, customer imports signed release manifest.
5. Runtime checks support window before enabling upgraded runtime.

## Rollback
- Keep previous image and previous license/manifest.
- If upgrade entitlement or runtime validation fails, redeploy previous image and restore previous manifest.
