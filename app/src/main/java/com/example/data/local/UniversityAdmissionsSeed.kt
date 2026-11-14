package com.example.data.local

import com.example.model.MajorFieldCategory
import com.example.model.UniversityAdmissionEntity
import com.example.model.UniversityFormulaType

object UniversityAdmissionsSeed {

    const val REFERENCE_DB_VERSION_LABEL = "إصدار القاعدة المرجعية: v3.0 • تحديث ذو القعدة 1448 هـ"
    const val DISCLAIMER_TEXT =
        "النسب المعروضة تمثل آخر نسب قبول معلنة للعام الماضي وتُستخدم للاسترشاد فقط، ويتم تحديث قاعدة البيانات شهرياً"

    fun defaultUniversityAdmissions(): List<UniversityAdmissionEntity> = listOf(
        // 1. جامعة الملك سعود (الرياض) - 30/30/40
        UniversityAdmissionEntity(
            id = "ksu_medicine",
            universityName = "جامعة الملك سعود",
            regionName = "الرياض",
            majorName = "الطب والجراحة (المسار الصحي)",
            fieldCategory = MajorFieldCategory.MEDICAL.name,
            formulaType = UniversityFormulaType.STANDARD_30_30_40.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 40% تحصيلي",
            stepRequirementNote = "لا يدخل STEP في الموزونة، ويُستفاد منه للإعفاء من مقرر اللغة الإنجليزية في السنة التحضيرية (درجة 85+)",
            lastYearMinWeighted = 95.40,
            requiresStepScore = false
        ),
        UniversityAdmissionEntity(
            id = "ksu_cs_ai",
            universityName = "جامعة الملك سعود",
            regionName = "الرياض",
            majorName = "علوم الحاسب والذكاء الاصطناعي",
            fieldCategory = MajorFieldCategory.TECH_CS.name,
            formulaType = UniversityFormulaType.STANDARD_30_30_40.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 40% تحصيلي",
            stepRequirementNote = "لا يشترط STEP في حساب الموزونة الأولية",
            lastYearMinWeighted = 91.80,
            requiresStepScore = false
        ),
        UniversityAdmissionEntity(
            id = "ksu_engineering",
            universityName = "جامعة الملك سعود",
            regionName = "الرياض",
            majorName = "الهندسة العامة (المسار العلمي والهندسي)",
            fieldCategory = MajorFieldCategory.ENGINEERING.name,
            formulaType = UniversityFormulaType.STANDARD_30_30_40.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 40% تحصيلي",
            stepRequirementNote = "التخصيص بعد اجتياز السنة الأولى المشتركة",
            lastYearMinWeighted = 89.60,
            requiresStepScore = false
        ),
        UniversityAdmissionEntity(
            id = "ksu_business_law",
            universityName = "جامعة الملك سعود",
            regionName = "الرياض",
            majorName = "إدارة الأعمال والحقوق (الأنظمة)",
            fieldCategory = MajorFieldCategory.ADMIN_LAW.name,
            formulaType = UniversityFormulaType.STANDARD_30_30_40.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 40% تحصيلي",
            stepRequirementNote = "لا يشترط STEP للقبول في الكليات الإنسانية والإدارية",
            lastYearMinWeighted = 86.20,
            requiresStepScore = false
        ),

        // 2. جامعة الملك فهد للبترول والمعادن (الظهران) - 50/50
        UniversityAdmissionEntity(
            id = "kfupm_ai_software",
            universityName = "جامعة الملك فهد للبترول والمعادن",
            regionName = "المنطقة الشرقية",
            majorName = "هندسة البرمجيات والذكاء الاصطناعي",
            fieldCategory = MajorFieldCategory.TECH_CS.name,
            formulaType = UniversityFormulaType.KFUPM_50_50.name,
            formulaDisplay = "50% قدرات + 50% تحصيلي",
            stepRequirementNote = "يشترط حد أدنى في اختبار STEP أو IELTS لتجاوز السنة التحضيرية أو القبول المبكر",
            lastYearMinWeighted = 95.50,
            requiresStepScore = false
        ),
        UniversityAdmissionEntity(
            id = "kfupm_engineering",
            universityName = "جامعة الملك فهد للبترول والمعادن",
            regionName = "المنطقة الشرقية",
            majorName = "الهندسة الكيميائية والميكانيكية والكهربائية",
            fieldCategory = MajorFieldCategory.ENGINEERING.name,
            formulaType = UniversityFormulaType.KFUPM_50_50.name,
            formulaDisplay = "50% قدرات + 50% تحصيلي",
            stepRequirementNote = "تعتمد الموزونة كلياً على القدرات والتحصيلي (50% لكل منهما)",
            lastYearMinWeighted = 93.50,
            requiresStepScore = false
        ),

        // 3. جامعة الملك عبدالعزيز (جدة) - 30/30/30/10 STEP
        UniversityAdmissionEntity(
            id = "kau_medicine_dentistry",
            universityName = "جامعة الملك عبدالعزيز",
            regionName = "مكة المكرمة",
            majorName = "الطب والجراحة وطب الأسنان",
            fieldCategory = MajorFieldCategory.MEDICAL.name,
            formulaType = UniversityFormulaType.KAU_30_30_30_10STEP.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 30% تحصيلي + 10% STEP",
            stepRequirementNote = "يدخل اختبار STEP بنسبة 10% مباشرة في معادلة القبول",
            lastYearMinWeighted = 93.80,
            requiresStepScore = true
        ),
        UniversityAdmissionEntity(
            id = "kau_pharmacy",
            universityName = "جامعة الملك عبدالعزيز",
            regionName = "مكة المكرمة",
            majorName = "دكتور صيدلة (PharmD)",
            fieldCategory = MajorFieldCategory.MEDICAL.name,
            formulaType = UniversityFormulaType.KAU_30_30_30_10STEP.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 30% تحصيلي + 10% STEP",
            stepRequirementNote = "يدخل اختبار STEP بنسبة 10% في معادلة القبول",
            lastYearMinWeighted = 91.20,
            requiresStepScore = true
        ),
        UniversityAdmissionEntity(
            id = "kau_engineering_cs",
            universityName = "جامعة الملك عبدالعزيز",
            regionName = "مكة المكرمة",
            majorName = "الهندسة وعلوم الحاسب وتقنية المعلومات",
            fieldCategory = MajorFieldCategory.ENGINEERING.name,
            formulaType = UniversityFormulaType.KAU_30_30_30_10STEP.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 30% تحصيلي + 10% STEP",
            stepRequirementNote = "يدخل اختبار STEP بنسبة 10%، ويُعفى من مقرر الإنجليزي عند تحقيق الدرجة المطلوبة",
            lastYearMinWeighted = 88.40,
            requiresStepScore = true
        ),

        // 4. جامعة طيبة (المدينة المنورة) وجامعة تبوك - 40/30/30
        UniversityAdmissionEntity(
            id = "taibah_medicine",
            universityName = "جامعة طيبة",
            regionName = "المدينة المنورة",
            majorName = "الطب والجراحة وطب الأسنان (المسار الطبي)",
            fieldCategory = MajorFieldCategory.MEDICAL.name,
            formulaType = UniversityFormulaType.TAIBAH_40_30_30.name,
            formulaDisplay = "40% ثانوية + 30% قدرات + 30% تحصيلي",
            stepRequirementNote = "تمنح جامعة طيبة وزناً أعلى للمعدل التراكمي للثانوية (40%)",
            lastYearMinWeighted = 95.10,
            requiresStepScore = false
        ),
        UniversityAdmissionEntity(
            id = "taibah_cs_eng",
            universityName = "جامعة طيبة",
            regionName = "المدينة المنورة",
            majorName = "هندسة وعلوم الحاسب والذكاء الاصطناعي",
            fieldCategory = MajorFieldCategory.TECH_CS.name,
            formulaType = UniversityFormulaType.TAIBAH_40_30_30.name,
            formulaDisplay = "40% ثانوية + 30% قدرات + 30% تحصيلي",
            stepRequirementNote = "لا يشترط STEP في حساب الموزونة الأساسية",
            lastYearMinWeighted = 89.20,
            requiresStepScore = false
        ),
        UniversityAdmissionEntity(
            id = "tabuk_medicine_eng",
            universityName = "جامعة تبوك",
            regionName = "تبوك",
            majorName = "الطب والجراحة والهندسة العامة",
            fieldCategory = MajorFieldCategory.MEDICAL.name,
            formulaType = UniversityFormulaType.TAIBAH_40_30_30.name,
            formulaDisplay = "40% ثانوية + 30% قدرات + 30% تحصيلي",
            stepRequirementNote = "معادلة 40% ثانوية + 30% قدرات + 30% تحصيلي",
            lastYearMinWeighted = 92.60,
            requiresStepScore = false
        ),

        // 5. جامعة الملك خالد (أبها - عسير) - 30/30/35/5 STEP & 15% STEP للغات
        UniversityAdmissionEntity(
            id = "kku_medicine_pharmacy",
            universityName = "جامعة الملك خالد",
            regionName = "عسير",
            majorName = "الطب والجراحة وطب الأسنان والصيدلة",
            fieldCategory = MajorFieldCategory.MEDICAL.name,
            formulaType = UniversityFormulaType.KKU_30_30_35_5STEP.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 35% تحصيلي + 5% STEP",
            stepRequirementNote = "يدخل اختبار STEP بنسبة 5% للتخصصات الصحية والهندسية والحاسب",
            lastYearMinWeighted = 93.40,
            requiresStepScore = true
        ),
        UniversityAdmissionEntity(
            id = "kku_cs_engineering",
            universityName = "جامعة الملك خالد",
            regionName = "عسير",
            majorName = "الهندسة وعلوم الحاسب والذكاء الاصطناعي",
            fieldCategory = MajorFieldCategory.TECH_CS.name,
            formulaType = UniversityFormulaType.KKU_30_30_35_5STEP.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 35% تحصيلي + 5% STEP",
            stepRequirementNote = "يدخل اختبار STEP بنسبة 5% في معادلة القبول",
            lastYearMinWeighted = 87.90,
            requiresStepScore = true
        ),
        UniversityAdmissionEntity(
            id = "kku_languages_translation",
            universityName = "جامعة الملك خالد",
            regionName = "عسير",
            majorName = "اللغات والترجمة (اللغة الإنجليزية)",
            fieldCategory = MajorFieldCategory.ADMIN_LAW.name,
            formulaType = UniversityFormulaType.KKU_LANG_30_30_25_15STEP.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 25% تحصيلي + 15% STEP",
            stepRequirementNote = "يدخل اختبار STEP بنسبة 15% لتخصص اللغات والترجمة",
            lastYearMinWeighted = 85.00,
            requiresStepScore = true
        ),

        // 6. جامعة أم القرى، الإمام محمد بن سعود، الجامعة الإسلامية، وجامعة القصيم - 30/30/40
        UniversityAdmissionEntity(
            id = "uqu_medicine_pharmacy",
            universityName = "جامعة أم القرى",
            regionName = "مكة المكرمة",
            majorName = "الطب والجراحة والصيدلة الإكلينيكية",
            fieldCategory = MajorFieldCategory.MEDICAL.name,
            formulaType = UniversityFormulaType.STANDARD_30_30_40.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 40% تحصيلي",
            stepRequirementNote = "يشترط تحقيق الحد الأدنى في اختبار STEP لبعض التخصصات الصحية والطبية",
            lastYearMinWeighted = 94.50,
            requiresStepScore = false
        ),
        UniversityAdmissionEntity(
            id = "imamu_cs_law",
            universityName = "جامعة الإمام محمد بن سعود الإسلامية",
            regionName = "الرياض",
            majorName = "علوم الحاسب، الهندسة، والشريعة والقانون",
            fieldCategory = MajorFieldCategory.TECH_CS.name,
            formulaType = UniversityFormulaType.STANDARD_30_30_40.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 40% تحصيلي",
            stepRequirementNote = "يشترط اختبار STEP للمسار الصحي واللغات والترجمة",
            lastYearMinWeighted = 88.75,
            requiresStepScore = false
        ),
        UniversityAdmissionEntity(
            id = "iu_engineering_law",
            universityName = "الجامعة الإسلامية بالمدينة المنورة",
            regionName = "المدينة المنورة",
            majorName = "الهندسة، الحاسب الآلي، والأنظمة",
            fieldCategory = MajorFieldCategory.ENGINEERING.name,
            formulaType = UniversityFormulaType.STANDARD_30_30_40.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 40% تحصيلي",
            stepRequirementNote = "تطبق المعادلة الثلاثية الموحدة (30/30/40)",
            lastYearMinWeighted = 87.30,
            requiresStepScore = false
        ),
        UniversityAdmissionEntity(
            id = "qu_medicine_dentistry",
            universityName = "جامعة القصيم",
            regionName = "القصيم",
            majorName = "الطب والجراحة وطب الأسنان",
            fieldCategory = MajorFieldCategory.MEDICAL.name,
            formulaType = UniversityFormulaType.STANDARD_30_30_40.name,
            formulaDisplay = "30% ثانوية + 30% قدرات + 40% تحصيلي",
            stepRequirementNote = "تطبق معادلة 30% ثانوية + 30% قدرات + 40% تحصيلي",
            lastYearMinWeighted = 93.90,
            requiresStepScore = false
        )
    )
}
