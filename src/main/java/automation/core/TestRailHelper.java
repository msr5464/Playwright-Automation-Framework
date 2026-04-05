package automation.core;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class TestRailHelper
{

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestResultObject
    {
        private String testName;
        private String testClass;
        private boolean passed;
        private String startTime;
        private String endTime;
        private String comment;
        private String screenshotLink;
    }
}
