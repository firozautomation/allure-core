package ru.yandex.qatools.allure.data.utils;

import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import static org.jvnet.jaxb2_commons.lang.StringUtils.isEmpty;

/**
 * eroshenkoam
 * 11/03/15
 */
public final class DescriptionUtils {

    DescriptionUtils() {
    }

    public static Description mergeDescriptions(TestSuiteResult testSuiteResult, TestCaseResult testCaseResult) {
        return mergeDescriptions(testSuiteResult.getDescription(), testCaseResult.getDescription());
    }

    public static Description mergeDescriptions(Description testSuiteDescription, Description testCaseDescription) {
        if (testSuiteDescription == null) {
            return testCaseDescription;
        }

        if (testCaseDescription == null) {
            return testSuiteDescription;
        }

        Description description = new Description();
        if (!isEmpty(testSuiteDescription.getValue())) {

            DescriptionType descriptionType = getDescriptionType(
                    testSuiteDescription.getType(),
                    testCaseDescription.getType()
            );

            String descriptionValue = descriptionType.equals(DescriptionType.MARKDOWN) ?
                    String.format("%s\n\n%s", testSuiteDescription.getValue(), testCaseDescription.getValue()) :
                    String.format("%s\n%s", testSuiteDescription.getValue(), testCaseDescription.getValue());

            description.setValue(descriptionValue);
            description.setType(descriptionType);
        }
        return description;
    }

    private static DescriptionType getDescriptionType(DescriptionType suiteType, DescriptionType testCaseType) {
        return suiteType.equals(DescriptionType.MARKDOWN) ?
                DescriptionType.MARKDOWN : testCaseType;
    }

}
