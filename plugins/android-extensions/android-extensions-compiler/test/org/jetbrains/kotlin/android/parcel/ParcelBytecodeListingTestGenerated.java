/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.android.parcel;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class ParcelBytecodeListingTestGenerated extends AbstractParcelBytecodeListingTest {
    public void testAllFilesPresentInCodegen() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
    }

    @TestMetadata("customDescribeContents.kt")
    public void testCustomDescribeContents() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/customDescribeContents.kt");
        doTest(fileName);
    }

    @TestMetadata("customParcelablesDifferentModule.kt")
    public void testCustomParcelablesDifferentModule() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/customParcelablesDifferentModule.kt");
        doTest(fileName);
    }

    @TestMetadata("customParcelablesSameModule.kt")
    public void testCustomParcelablesSameModule() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/customParcelablesSameModule.kt");
        doTest(fileName);
    }

    @TestMetadata("customSimple.kt")
    public void testCustomSimple() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/customSimple.kt");
        doTest(fileName);
    }

    @TestMetadata("customSimpleWithNewArray.kt")
    public void testCustomSimpleWithNewArray() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/customSimpleWithNewArray.kt");
        doTest(fileName);
    }

    @TestMetadata("describeContentsFromSuperType.kt")
    public void testDescribeContentsFromSuperType() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/describeContentsFromSuperType.kt");
        doTest(fileName);
    }

    @TestMetadata("duplicatingClinit.kt")
    public void testDuplicatingClinit() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/duplicatingClinit.kt");
        doTest(fileName);
    }

    @TestMetadata("IBinderIInterface.kt")
    public void testIBinderIInterface() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/IBinderIInterface.kt");
        doTest(fileName);
    }

    @TestMetadata("listInsideList.kt")
    public void testListInsideList() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/listInsideList.kt");
        doTest(fileName);
    }

    @TestMetadata("nullableNotNullSize.kt")
    public void testNullableNotNullSize() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/nullableNotNullSize.kt");
        doTest(fileName);
    }

    @TestMetadata("parcelable.kt")
    public void testParcelable() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/parcelable.kt");
        doTest(fileName);
    }

    @TestMetadata("serializable.kt")
    public void testSerializable() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/serializable.kt");
        doTest(fileName);
    }

    @TestMetadata("simple.kt")
    public void testSimple() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/simple.kt");
        doTest(fileName);
    }

    @TestMetadata("simpleList.kt")
    public void testSimpleList() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/simpleList.kt");
        doTest(fileName);
    }

    @TestMetadata("size.kt")
    public void testSize() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-compiler/testData/parcel/codegen/size.kt");
        doTest(fileName);
    }
}
