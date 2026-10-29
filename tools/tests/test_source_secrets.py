import importlib.util,unittest
from pathlib import Path
spec=importlib.util.spec_from_file_location('secret_audit',Path(__file__).parents[1]/'audit_source_secrets.py')
module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
class SecretAuditTest(unittest.TestCase):
 def test_private_keys_are_never_printed_or_accepted(self):
  self.assertIn('private-key',module.findings('test.txt',b'-----BEGIN '+'PRIVATE KEY-----'.encode()))
 def test_signing_and_env_files_are_rejected(self):
  for name in ('release.jks','.env.production','antireel-signing.properties','local.properties'):
   self.assertIn('forbidden-secret-file',module.findings(name,b''))
 def test_template_is_allowed_but_real_password_is_not(self):
  self.assertEqual([],module.findings('example',b'storePassword=REPLACE_LOCALLY'))
  self.assertIn('assigned-secret',module.findings('example',b'storePassword=unit-test-secret'))
if __name__=='__main__':unittest.main()
