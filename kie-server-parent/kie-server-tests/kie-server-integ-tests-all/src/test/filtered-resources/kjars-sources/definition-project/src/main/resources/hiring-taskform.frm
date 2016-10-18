{
  "id": "011ebd63-f9b5-4057-b5ff-999c1c7e5080",
  "name": "hiring-taskform.frm",
  "model": {
    "processId": "hiring",
    "processName": "Hiring a Developer",
    "variables": [
      {
        "name": "name",
        "type": "java.lang.String"
      },
      {
        "name": "age",
        "type": "java.lang.Integer"
      },
      {
        "name": "twitter",
        "type": "java.lang.String"
      },
      {
        "name": "offering",
        "type": "java.lang.Integer"
      },
      {
        "name": "skills",
        "type": "java.lang.String"
      },
      {
        "name": "mail",
        "type": "java.lang.String"
      },
      {
        "name": "tech_score",
        "type": "java.lang.Integer"
      },
      {
        "name": "hr_score",
        "type": "java.lang.Integer"
      },
      {
        "name": "signed",
        "type": "java.lang.Boolean"
      }
    ],
    "formModelType": "org.kie.workbench.common.forms.jbpm.model.authoring.process.BusinessProcessFormModel"
  },
  "fields": [
    {
      "maxLength": 100,
      "placeHolder": "",
      "annotatedId": false,
      "code": "TextBox",
      "id": "field_2225717094101704E12",
      "name": "name",
      "label": "Candidate Name",
      "required": false,
      "readonly": false,
      "validateOnChange": true,
      "binding": "name",
      "standaloneClassName": "java.lang.String",
      "serializedFieldClassName": "org.kie.workbench.common.forms.model.impl.basic.textBox.TextBoxFieldDefinition"
    }
  ],
  "layoutTemplate": {
    "version": 1,
    "layoutProperties": {},
    "rows": [
      {
        "layoutColumns": [
          {
            "span": "12",
            "rows": [],
            "layoutComponents": [
              {
                "dragTypeName": "org.kie.workbench.common.forms.editor.client.editor.rendering.EditorFieldLayoutComponent",
                "properties": {
                  "field_id": "field_2225717094101704E12",
                  "form_id": "011ebd63-f9b5-4057-b5ff-999c1c7e5080"
                }
              }
            ]
          }
        ]
      }
    ]
  }
}
