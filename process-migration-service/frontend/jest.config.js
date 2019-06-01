/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// eslint-disable-next-line no-undef
module.exports = {
  reporters: [
    "default",
    [
      "jest-junit",
      {
        /*
         * Removes spaces from test titles and makes first
         * letter of each word capitalized.
         *
         * unit test -> UnitTest
         *
         * See junit.xml report for resulting look.
         */
        titleTemplate: vars => {
          var str = vars.title.toLowerCase();
          str = str.split(" ");
          for (var i = 0; i < str.length; i++) {
            str[i] = str[i].charAt(0).toUpperCase() + str[i].slice(1);
          }
          var result = str.join("");
          return result.replace(",");
        },
        outputDirectory: "../target/jest-junit-reports",
        outputName: "TEST-org.kie.processmigration.js.tests.xml",
        suiteName: "org.kie.processmigration.js.tests",
        suiteNameTemplate: "{filename}",
        /*
         * Jenkins JUnit Plugin cares only about classname.
         * It cuts its name into 2 parts:
         * Part before the last period is considered as a package.
         * Part after the last period is considered as a test class name.
         */
        classNameTemplate: vars => {
          var str = vars.filename;
          str = str.slice(0, str.length - ".test.js".length);
          return "org.kie.processmigration.js.tests." + str;
        }
      }
    ]
  ],
  moduleDirectories: ["node_modules", "src"],
  moduleFileExtensions: ["js", "jsx"],
  testRegex: "/__tests__/.*\\.test\\.(jsx?)$",
  transform: {
    "^.+\\.jsx?$": "babel-jest"
  }
};
