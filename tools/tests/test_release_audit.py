import struct,sys,unittest
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from audit_release_apk import elf_load_alignments,excludes_all_app_data,BACKUP_DOMAINS

class ElfAuditTests(unittest.TestCase):
    def elf(self,alignment):
        data=bytearray(120);data[:6]=b'\x7fELF\x02\x01'
        struct.pack_into('<Q',data,32,64);struct.pack_into('<HH',data,54,56,1)
        struct.pack_into('<I',data,64,1);struct.pack_into('<Q',data,112,alignment)
        return bytes(data)
    def test_distinguishes_old_4k_and_16k_libraries(self):
        self.assertEqual([4096],elf_load_alignments(self.elf(4096)))
        self.assertEqual([16384],elf_load_alignments(self.elf(16384)))
    def test_rejects_truncated_or_non_elf_input(self):
        for value in [b'',b'not a native library',self.elf(16384)[:80]]:
            with self.assertRaises(ValueError):elf_load_alignments(value)

class BackupAuditTests(unittest.TestCase):
    def exclusions(self,parent):
        return [{'tag':'exclude','parent':parent,'attrs':{'domain':domain,'path':'.'}} for domain in sorted(BACKUP_DOMAINS)]
    def test_requires_every_app_data_domain_to_be_excluded(self):
        nodes=self.exclusions('device-transfer')
        self.assertTrue(excludes_all_app_data(nodes,'device-transfer'))
        self.assertFalse(excludes_all_app_data(nodes[:-1],'device-transfer'))
    def test_cloud_rules_cannot_substitute_for_device_transfer_rules(self):
        nodes=self.exclusions('cloud-backup')
        self.assertTrue(excludes_all_app_data(nodes,'cloud-backup'))
        self.assertFalse(excludes_all_app_data(nodes,'device-transfer'))

if __name__=='__main__':unittest.main()
