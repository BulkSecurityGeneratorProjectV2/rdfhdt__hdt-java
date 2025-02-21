package org.rdfhdt.hdt.hdtDiff;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.rdfhdt.hdt.compact.bitmap.BitmapFactory;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.hdt.writer.TripleWriterHDT;
import org.rdfhdt.hdt.hdtDiff.utils.EmptyBitmap;
import org.rdfhdt.hdt.options.HDTOptions;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdt.triples.impl.utils.HDTTestUtils;
import org.rdfhdt.hdt.util.io.AbstractMapMemoryTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class HdtDiffTest extends AbstractMapMemoryTest {
    public static class DictionaryTestData {
        public final String dictionaryType;
        public final String dictionaryTempType;

        public DictionaryTestData(String dictionaryType, String dictionaryTempType) {
            this.dictionaryType = dictionaryType;
            this.dictionaryTempType = dictionaryTempType;
        }
    }
    /**
     * Return type for {@link #createTestHDT(File, File, File, HDTOptions, int, int, int, int, int)}
     */
    public static class HDTDiffData {
        public int tupleCountA;
        public int tupleCountB;
        public int tupleCountC;
        public int shared;
    }

    public static final DictionaryTestData[] DICTIONARY_TEST_DATA = {
            new DictionaryTestData(HDTVocabulary.DICTIONARY_TYPE_FOUR_SECTION, DictionaryFactory.MOD_DICT_IMPL_HASH),
            new DictionaryTestData(DictionaryFactory.DICTIONARY_TYPE_FOUR_SECTION_BIG, DictionaryFactory.MOD_DICT_IMPL_HASH),
            new DictionaryTestData(DictionaryFactory.DICTIONARY_TYPE_MULTI_OBJECTS, DictionaryFactory.MOD_DICT_IMPL_MULT_HASH),
            new DictionaryTestData(HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION, DictionaryFactory.MOD_DICT_IMPL_HASH_PSFC)
    };

    /**
     * create 2 test hdts and a result hdt
     *
     * @param fileName   hdt file name
     * @param fileName2  hdt 2 file name
     * @param fileName3  result hdt file name
     * @param subjects   number of subjects
     * @param predicates number of predicates
     * @param objects    number of objects
     * @param shared     number of shared subjects/objects
     * @return tuple count
     */
    public static HDTDiffData createTestHDT(File fileName, File fileName2, File fileName3, HDTOptions spec, int subjects, int predicates, int objects, int shared, int shift) throws IOException {
        String baseURI = "http://ex.org/";
        HDTDiffData count = new HDTDiffData();
        int shiftCount = 0;
        try (final TripleWriterHDT wr = new TripleWriterHDT(baseURI, spec, fileName.getAbsolutePath(), false);
             final TripleWriterHDT wr2 = new TripleWriterHDT(baseURI, spec, fileName2.getAbsolutePath(), false);
             final TripleWriterHDT wr3 = new TripleWriterHDT(baseURI, spec, fileName3.getAbsolutePath(), false)) {
            for (int i = subjects; i > 0; i--) {
                String subject;
                if (i <= shared) {
                    subject = baseURI + "Shared" + i;
                } else {
                    subject = baseURI + "Subject" + (i - shared);
                }

                for (int j = predicates; j > 0; j--) {
                    String predicate = baseURI + "Predicate" + j;
                    for (int k = objects; k > 0; k--) {
                        String object;
                        if (k <= shared) {
                            object = baseURI + "Shared" + k;
                        } else {
                            object = baseURI + "Object" + (k - shared);
                        }
                        shiftCount = (shiftCount + 1) % shift;
                        int newtriple = 0;

                        if (shiftCount != 0) {
                            wr.addTriple(new TripleString(
                                    subject,
                                    predicate,
                                    object
                            ));
                            count.tupleCountA++;
                            newtriple++;
                        }
                        if (shiftCount == 0 || shiftCount == 1) {
                            wr2.addTriple(new TripleString(
                                    subject,
                                    predicate,
                                    object
                            ));
                            count.tupleCountB++;
                            newtriple++;
                        } else {
                            if (newtriple == 1) {
                                wr3.addTriple(new TripleString(
                                        subject,
                                        predicate,
                                        object
                                ));
                                count.tupleCountC++;
                            }
                        }

                        if (newtriple == 2) {

                            count.shared++;
                        }
                    }
                }
            }
        }

        Assert.assertEquals("|A-B| != |C|", count.tupleCountA - count.shared, count.tupleCountC);

        return count;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> genParam() {
        List<Object[]> list = new ArrayList<>();
        for (DictionaryTestData data : DICTIONARY_TEST_DATA) {
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 25, 50, 37, 12 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 25, 50, 37, 0 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 25, 50, 0, 12 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 25, 50, 0, 0 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 25, 0, 37, 12 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 25, 0, 37, 0 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 25, 0, 0, 12 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 25, 0, 0, 0 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 0, 50, 37, 12 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 0, 50, 37, 0 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 0, 50, 0, 12 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 0, 50, 0, 0 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 0, 0, 37, 12 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 0, 0, 37, 0 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 0, 0, 0, 12 });
            list.add(new Object[] { data.dictionaryType, data.dictionaryTempType, 0, 0, 0, 0 });
        }
        return list;
    }

    public static void assertHdtEquals(HDT hdt1, HDT hdt2) {
        Assert.assertEquals(
                "HDT Same triple count",
                hdt1.getTriples().getNumberOfElements(),
                hdt2.getTriples().getNumberOfElements()
        );

        IteratorTripleString its1;
        IteratorTripleString its2;

        try {
            its1 = hdt1.search("", "", "");
            its2 = hdt1.search("", "", "");
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }

        while (true) {
            if (!its1.hasNext()) {
                Assert.assertFalse("its1 is empty, but not its2", its2.hasNext());
                return;
            }
            Assert.assertTrue("its2 is empty, but not its1", its2.hasNext());

            TripleString ts1 = its1.next();
            TripleString ts2 = its2.next();
            Assert.assertEquals("ts1=ts2", ts1, ts2);
        }
    }

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    final HDTSpecification spec;
    final int shared;
    final int subjects;
    final int predicates;
    final int objects;

    public HdtDiffTest(String dictionaryType, String tempDictionaryImpl, int subjects, int predicates, int objects, int shared) {
        spec = new HDTSpecification();
        spec.set("dictionary.type", dictionaryType);
        spec.set("tempDictionary.impl", tempDictionaryImpl);
        this.subjects = subjects;
        this.predicates = predicates;
        this.objects = objects;
        this.shared = shared;
    }

    @Test
    public void hdtDiffTest() throws IOException {
        File hdtFile1 = tempDir.newFile();
        File hdtFile2 = tempDir.newFile();
        File hdtDiffExceptedFile = tempDir.newFile();
        createTestHDT(hdtFile1, hdtFile2, hdtDiffExceptedFile, spec, subjects, predicates, objects, shared, 4);

        try (HDT hdtDiffExcepted = HDTManager.mapHDT(hdtDiffExceptedFile.getAbsolutePath(), null, spec);
            HDT hdtDiffActual = HDTManager.diffHDT(hdtFile1.getAbsolutePath(), hdtFile2.getAbsolutePath(), spec, null)) {

            Assert.assertEquals(
                    "Dictionaries aren't the same",
                    hdtDiffExcepted.getDictionary().getType(),
                    hdtDiffActual.getDictionary().getType()
            );

            assertHdtEquals(hdtDiffExcepted, hdtDiffActual);
        }
    }

    @Test
    public void diffHDTBitIdentityTest() throws IOException {
        File f = tempDir.newFile();
        File location = tempDir.newFile();
        try (HDTTestUtils hdtTestUtils = new HDTTestUtils(f, subjects, predicates, objects, shared, spec, true)) {

            HDT hdtDiffExcepted = hdtTestUtils.hdt;
            try (HDT hdtDiffActual = HDTManager.diffHDTBit(location.getAbsolutePath(), f.getAbsolutePath(), new EmptyBitmap(hdtTestUtils.triples), spec, null)) {

                Assert.assertEquals(
                        "Dictionaries aren't the same",
                        hdtDiffExcepted.getDictionary().getType(),
                        hdtDiffActual.getDictionary().getType()
                );

                assertHdtEquals(hdtTestUtils.hdt, hdtDiffActual);
            }
        }
    }
    @Test
    public void diffHDTIdentityTest() throws IOException {
        File f = tempDir.newFile();
        File f2 = tempDir.newFile();
        try (HDTTestUtils hdtTestUtils = new HDTTestUtils(f, subjects, predicates, objects, shared, spec, true)) {
            new HDTTestUtils(f2, 0, 0, 0, 0, spec, true).close();

            HDT hdtDiffExcepted = hdtTestUtils.hdt;
            try (HDT hdtDiffActual = HDTManager.diffHDT(f.getAbsolutePath(), f2.getAbsolutePath(), spec, null)) {

                Assert.assertEquals(
                        "Dictionaries aren't the same",
                        hdtDiffExcepted.getDictionary().getType(),
                        hdtDiffActual.getDictionary().getType()
                );

                assertHdtEquals(hdtTestUtils.hdt, hdtDiffActual);
            }
        }

    }
}
