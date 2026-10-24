// Verify both trust stores used by Node and OS-linked HTTP clients in released images.
const { X509Certificate } = require('node:crypto');
const { readFileSync } = require('node:fs');
const { rootCertificates } = require('node:tls');
const blocked = new Set([
  '86a1ecba089c4a8d3bbe2734c612ba341d813e043cf9e8a862cd5c57a36bbe6b',
  'd7a7a0fb5d7e2731d771e9484ebcdef71d5f0c3e0a2948782bc83ee0ea699ef4',
]);
function verify(name, certificates) {
  if (!certificates?.length) throw new Error(`${name}: empty certificate bundle`);
  for (const pem of certificates) {
    const cert = new X509Certificate(pem);
    if (blocked.has(cert.fingerprint256.replaceAll(':', '').toLowerCase())) {
      throw new Error(`${name}: distrusted certificate ${cert.subject}`);
    }
  }
  console.log(`${name}: ${certificates.length} certificates; distrusted roots absent`);
}
verify('Node bundled roots', rootCertificates);
for (const file of process.argv.slice(2)) {
  verify(file, readFileSync(file, 'utf8').match(/-----BEGIN CERTIFICATE-----[\s\S]*?-----END CERTIFICATE-----/g));
}
