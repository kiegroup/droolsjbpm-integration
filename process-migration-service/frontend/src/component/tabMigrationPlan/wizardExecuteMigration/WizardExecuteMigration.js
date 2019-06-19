import React from "react";
import axios from "axios";

import { Wizard } from "patternfly-react";
import { Button } from "patternfly-react";
import { Icon } from "patternfly-react";

import { ExecuteMigrationItems } from "../../common/WizardItems";
import { BACKEND_URL } from "../../common/PimConstants";

import WizardBase from "../WizardBase";
import { renderWizardSteps } from "../PfWizardRenderers";

import PageMigrationRunningInstances from "./PageMigrationRunningInstances";
import PageMigrationScheduler from "./PageMigrationScheduler";
import PageReview from "../PageReview";

export default class WizardExecuteMigration extends WizardBase {
  constructor(props) {
    super(props);
    this.state = {
      activeStepIndex: 0,
      activeSubStepIndex: 0,
      runningInstanceIds: "",
      scheduledStartTime: "",
      migrationDefinitionJsonStr: "",
      callbackUrl: "",
      pimServiceResponseJsonStr: ""
    };
  }

  //using Ref, this is called from parent before open the wizard to reset all the states.
  resetWizardStates() {
    this.setState({
      activeStepIndex: 0,
      activeSubStepIndex: 0,
      runningInstanceIds: "",
      scheduledStartTime: "",
      migrationDefinitionJsonStr: "",
      callbackUrl: "",
      pimServiceResponseJsonStr: ""
    });
  }

  onSubmitMigrationPlan = () => {
    const plan = this.state.migrationDefinitionJsonStr;

    //need to create a temp variable "self" to store this, so I can invoke this inside axios call
    const self = this;

    const serviceUrl = BACKEND_URL + "/migrations";
    axios
      .post(serviceUrl, plan, {
        headers: {
          "Content-Type": "application/json"
        }
      })
      .then(function(response) {
        self.setState({
          pimServiceResponseJsonStr: JSON.stringify(response.data, null, 2)
        });
        self.onNextButtonClick();
      });
  };

  convertFormDataToJson() {
    const execution = {
      type: "ASYNC"
    };

    if (
      this.state.scheduledStartTime !== null &&
      this.state.scheduledStartTime !== ""
    ) {
      execution.scheduledStartTime = this.state.scheduledStartTime;
    }
    if (this.state.callbackUrl !== null && this.state.callbackUrl !== "") {
      execution.callbackUrl = this.state.callbackUrl;
    }

    const formData = {
      planId: this.props.planId,
      kieServerId: this.props.kieServerId,
      processInstanceIds: "[" + this.state.runningInstanceIds + "]",
      execution: execution
    };

    var jsonStr = JSON.stringify(formData, null, 2);

    //Remove the " " from running instances because it's not a string
    if (jsonStr !== null && jsonStr !== "") {
      //replace "[ to [
      jsonStr = jsonStr.replace('"[', "[");

      //replace ]" to ]
      jsonStr = jsonStr.replace(']"', "]");
    }

    this.setState({ migrationDefinitionJsonStr: jsonStr });
  }

  setRunngingInstancesIds = ids => {
    this.setState({
      runningInstanceIds: ids
    });
  };

  setScheduleStartTime = startTime => {
    this.setState({
      scheduledStartTime: startTime
    });
  };

  setCallbackUrl = url => {
    this.setState({
      callbackUrl: url
    });
  };

  render() {
    const { activeStepIndex, activeSubStepIndex } = this.state;

    const renderExecuteMigrationWizardContents = wizardSteps => {
      return wizardSteps.map((step, stepIndex) =>
        step.subSteps.map((sub, subStepIndex) => {
          if (stepIndex === 0) {
            // render steps 1
            return (
              <Wizard.Contents
                key={subStepIndex}
                stepIndex={stepIndex}
                subStepIndex={subStepIndex}
                activeStepIndex={activeStepIndex}
                activeSubStepIndex={activeSubStepIndex}
              >
                <PageMigrationRunningInstances
                  runningInstances={this.props.runningInstances}
                  setRunngingInstancesIds={this.setRunngingInstancesIds}
                />
              </Wizard.Contents>
            );
          } else if (stepIndex === 1) {
            // render steps 2
            return (
              <Wizard.Contents
                key={subStepIndex}
                stepIndex={stepIndex}
                subStepIndex={subStepIndex}
                activeStepIndex={activeStepIndex}
                activeSubStepIndex={activeSubStepIndex}
              >
                <PageMigrationScheduler
                  setCallbackUrl={this.setCallbackUrl}
                  setScheduleStartTime={this.setScheduleStartTime}
                />
              </Wizard.Contents>
            );
          } else if (stepIndex === 2) {
            // render review
            return (
              <Wizard.Contents
                key={subStepIndex}
                stepIndex={stepIndex}
                subStepIndex={subStepIndex}
                activeStepIndex={activeStepIndex}
                activeSubStepIndex={activeSubStepIndex}
              >
                <PageReview
                  inputJsonStr={this.state.migrationDefinitionJsonStr}
                />
              </Wizard.Contents>
            );
          } else if (stepIndex === 3) {
            // render result page
            return (
              <Wizard.Contents
                key={subStepIndex}
                stepIndex={stepIndex}
                subStepIndex={subStepIndex}
                activeStepIndex={activeStepIndex}
                activeSubStepIndex={activeSubStepIndex}
              >
                <PageReview
                  inputJsonStr={this.state.pimServiceResponseJsonStr}
                />
              </Wizard.Contents>
            );
          }
          return null;
        })
      );
    };

    return (
      <div>
        <form className="form-horizontal" name="form_migration">
          <Wizard
            show={this.props.showMigrationWizard}
            onHide={this.props.closeMigrationWizard}
          >
            <Wizard.Header
              onClose={this.props.closeMigrationWizard}
              title="Execute Migration Plan Wizard"
            />
            <Wizard.Body>
              <Wizard.Steps
                steps={renderWizardSteps(
                  ExecuteMigrationItems,
                  activeStepIndex,
                  activeSubStepIndex,
                  this.onStepClick
                )}
              />
              <Wizard.Row>
                <Wizard.Main>
                  {renderExecuteMigrationWizardContents(
                    ExecuteMigrationItems,
                    this.state,
                    this.setInfo
                  )}
                </Wizard.Main>
              </Wizard.Row>
            </Wizard.Body>
            <Wizard.Footer>
              <Button
                bsStyle="default"
                className="btn-cancel"
                onClick={this.props.closeMigrationWizard}
              >
                Cancel
              </Button>
              <Button
                bsStyle="default"
                disabled={activeStepIndex === 0 && activeSubStepIndex === 0}
                onClick={this.onBackButtonClick}
              >
                <Icon type="fa" name="angle-left" />
                Back
              </Button>
              {(activeStepIndex === 0 || activeStepIndex === 1) && (
                <Button
                  bsStyle="primary"
                  disabled={this.state.runningInstanceIds.trim() == ""}
                  onClick={this.onNextButtonClick}
                >
                  Next
                  <Icon type="fa" name="angle-right" />
                </Button>
              )}
              {activeStepIndex === 2 && (
                <Button bsStyle="primary" onClick={this.onSubmitMigrationPlan}>
                  Execute Plan
                  <Icon type="fa" name="angle-right" />
                </Button>
              )}
              {activeStepIndex === 3 && (
                <Button
                  bsStyle="primary"
                  onClick={this.props.closeMigrationWizard}
                >
                  Close
                  <Icon type="fa" name="angle-right" />
                </Button>
              )}
            </Wizard.Footer>
          </Wizard>
        </form>
      </div>
    );
  }
}
