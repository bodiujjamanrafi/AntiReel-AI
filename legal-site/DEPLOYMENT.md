# Cloudflare deployment plan — approval and account access required

Prepared for `legal.imrafi.bond` only. No records, VPS, nameservers or existing sites have been changed. The four public URLs could not be reached during preparation, and TLS has not been verified. Cloudflare credentials/CLI/browser access are not available to this session.

## Review before publication

Review the four JSON documents in `content/` and confirm the developer identity matching Play Console, intended audience, any chosen governing law, support email retention/processor and the final hosting log/security configuration. Current documents deliberately do not invent a company, jurisdiction, retention SLA or age-verification system. See `../docs/PLAY_COMPLIANCE_1.0.22.md`.

Approve this exact external scope before an agent deploys: create a standalone static Cloudflare Pages project, upload this site's generated files, attach only `legal.imrafi.bond`, and create/update only the `legal` CNAME for the `imrafi.bond` zone. Keep the apex and all VPS/mail/other DNS records unchanged. DNS target must be the actual Pages project hostname returned by Cloudflare, not an assumed or invented name.

## Dashboard route (no tokens in the project)

1. Run `python3 legal-site/build.py` from the Android project root. The deployment directory is `legal-site/dist/`; the ready ZIP is `dist/AntiReel-legal-pages-1.0.22.zip`.
2. Sign in to the Cloudflare account that controls `imrafi.bond`. In Workers & Pages create a **Pages / Direct Upload** static project. Suggested name: `antireel-legal` (availability not verified). Upload the files/ZIP; no framework, Node dependencies or server are required.
3. Check all four routes on the actual `*.pages.dev` hostname Cloudflare returns. Content must be public, without Cloudflare Access/login, Turnstile or a challenge that prevents a reviewer reading it.
4. In that Pages project's **Custom domains**, add `legal.imrafi.bond`. For a zone in the same account, follow Cloudflare's specific DNS prompt. The intended record is:

   | Type | Name | Target | TTL |
   | --- | --- | --- | --- |
   | CNAME | legal | Exact Pages hostname returned by this project | Auto |

   Review any EXISTING `legal` record before changing it. Do not touch `@`, `www`, mail records, VPS records, nameservers or unrelated zones. Do not point `legal` at the VPS. Associate the domain in Pages before manually adding a CNAME.
5. Wait for the custom domain to become Active and the managed HTTPS certificate to issue. Confirm no SSL/CAA/DNS conflict; do not weaken TLS or disable device security to work around one.
6. Keep Web Analytics/Zaraz/Browser Insights/custom scripts disabled for this site unless the privacy policy and Data Safety review are updated. No application cookies or external resources are included. Hosting/security logs are controlled by Cloudflare/account settings, not this static source.
7. Verify from an ordinary logged-out browser/mobile connection and run the verifier below. Test English text selection, mobile navigation, both system color schemes and keyboard focus. Do not submit an unavailable legal URL to Play Console.

## Post-deployment verification

```sh
python3 tools/verify_legal_urls.py --output /tmp/antireel-public-legal-check.json
```

It checks all four HTTPS pages, normal certificate validation, the expected host/content, support email, HTML content type, security headers and absence of cookies. It never changes DNS or hosting. A failed or unrun check is not successful deployment. Keep the successful result and date with the final release report.

## Optional authenticated CLI route

If you prefer Wrangler, use its official installation/login workflow on your own machine, then `wrangler pages project create` and `wrangler pages deploy legal-site/dist --project-name <actual-project-name>`. Custom-domain association and approval of the specific DNS change are still required. Do not paste API tokens into source, shell history, chat or the repository. No Wrangler installation, login or deployment was performed here.

## Authoritative references checked 29 October 2026

- https://developers.cloudflare.com/pages/get-started/direct-upload/
- https://developers.cloudflare.com/pages/configuration/custom-domains/
