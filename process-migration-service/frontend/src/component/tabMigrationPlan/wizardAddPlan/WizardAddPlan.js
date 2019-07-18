import React from "react";
import PropTypes from "prop-types";

import { Wizard, Button, Icon } from "patternfly-react";

import WizardBase from "../WizardBase";
import PageReview from "../PageReview";
import { renderWizardSteps } from "../PfWizardRenderers";

import PageMapping from "./PageMapping";
import PageDefinition from "./PageDefinition";
import PagePlanName from "./PagePlanName";

import { AddPlanItems } from "../../common/WizardItems";
import Notification from "../../Notification";
import { ALERT_TYPE_ERROR } from "patternfly-react/dist/js/components/Alert/AlertConstants";

export default class WizardAddPlan extends WizardBase {
  constructor(props) {
    super(props, AddPlanItems);
    this.state = {
      activeStepIndex: 0,
      activeSubStepIndex: 0,
      sourceInfo: {},
      targetInfo: {},
      wizardHeaderTitle: this.props.plan.id
        ? "Edit migration plan"
        : "Add migration plan",
      stepValidation: {}
    };
  }

  setSourceDefinition = sourceInfo => this.setState({ sourceInfo });
  setTargetDefinition = targetInfo => this.setState({ targetInfo });

  onChangeSource = (newContainerId, newProcessId) => {
    const source = {
      containerId: newContainerId,
      processId: newProcessId
    };
    this.onPlanFieldChange("source", source);
    this.onPlanFieldChange("mappings", {});
  };

  onChangeTarget = (newContainerId, newProcessId) => {
    const target = {
      containerId: newContainerId,
      processId: newProcessId
    };
    this.onPlanFieldChange("target", target);
    this.onPlanFieldChange("mappings", {});
  };

  onPlanFieldChange = (field, newValue) => {
    const plan = this.props.plan;
    if (newValue === null) {
      delete plan[field];
    } else {
      plan[field] = newValue;
    }
    this.props.onPlanChanged(plan);
  };

  setStepIsValid = (step, isValid) => {
    const { stepValidation } = this.state;
    stepValidation[step] = isValid;
    this.setState({ stepValidation });
  };

  isStepValid = step => {
    return this.state.stepValidation[step];
  };

  submitPlan = () => {
    this.props
      .onSavePlan()
      .then(() => this.onNextButtonClick())
      .catch(() =>
        this.setState({ errorMsg: "Unable to submit the migration plan" })
      );
  };

  render() {
    const {
      activeStepIndex,
      activeSubStepIndex,
      sourceInfo,
      targetInfo
    } = this.state;

    const renderAddPlanWizardContents = wizardSteps => {
      return wizardSteps.map((step, stepIndex) =>
        step.subSteps.map((sub, subStepIndex) => {
          if (stepIndex === 0) {
            // render steps 0
            return (
              <Wizard.Contents
                key={subStepIndex}
                stepIndex={stepIndex}
                subStepIndex={subStepIndex}
                activeStepIndex={activeStepIndex}
                activeSubStepIndex={activeSubStepIndex}
              >
                <PagePlanName
                  plan={this.props.plan}
                  onFieldChanged={this.onPlanFieldChange}
                  onIsValid={isValid => this.setStepIsValid(0, isValid)}
                />
              </Wizard.Contents>
            );
          } else if (stepIndex === 1) {
            // render steps 1
            return (
              <Wizard.Contents
                key={subStepIndex}
                stepIndex={stepIndex}
                subStepIndex={subStepIndex}
                activeStepIndex={activeStepIndex}
                activeSubStepIndex={activeSubStepIndex}
              >
                <PageDefinition
                  sourceInfo={sourceInfo}
                  targetInfo={targetInfo}
                  setSourceDefinition={this.setSourceDefinition}
                  setTargetDefinition={this.setTargetDefinition}
                  plan={this.props.plan}
                  onChangeSource={this.onChangeSource}
                  onChangeTarget={this.onChangeTarget}
                  kieServerId={this.props.kieServerId}
                  onIsValid={isValid => this.setStepIsValid(1, isValid)}
                />
              </Wizard.Contents>
            );
          } else if (stepIndex === 2) {
            // render steps 2
            return (
              <Wizard.Contents
                key={subStepIndex}
                stepIndex={stepIndex}
                subStepIndex={subStepIndex}
                activeStepIndex={activeStepIndex}
                activeSubStepIndex={activeSubStepIndex}
              >
                <PageMapping
                  plan={this.props.plan}
                  sourceProcess={this.state.sourceInfo}
                  targetProcess={this.state.targetInfo}
                  onMappingsChange={mappings =>
                    this.onPlanFieldChange("mappings", mappings)
                  }
                  onIsValid={isValid => this.setStepIsValid(2, isValid)}
                />
              </Wizard.Contents>
            );
          } else if (stepIndex === 3) {
            // render review
            return (
              <Wizard.Contents
                key={subStepIndex}
                stepIndex={stepIndex}
                subStepIndex={subStepIndex}
                activeStepIndex={activeStepIndex}
                activeSubStepIndex={activeSubStepIndex}
              >
                {this.state.errorMsg && (
                  <Notification
                    type={ALERT_TYPE_ERROR}
                    message={this.state.errorMsg}
                    onDismiss={() => this.setState({ errorMsg: "" })}
                  />
                )}
                <PageReview
                  object={this.props.plan}
                  exportedFileName={this.props.plan.name}
                  errorMsg={this.state.errorMsg}
                />
              </Wizard.Contents>
            );
          } else if (stepIndex === 4) {
            return (
              <Wizard.Contents
                key={subStepIndex}
                stepIndex={stepIndex}
                subStepIndex={subStepIndex}
                activeStepIndex={activeStepIndex}
                activeSubStepIndex={activeSubStepIndex}
              >
                <PageReview
                  object={this.props.plan}
                  exportedFileName={this.props.plan.name}
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
        <form
          className="form-horizontal"
          name="form_migration_plan"
          id="WizardAddPlan_id_form1"
        >
          <Wizard show={true} onHide={this.props.closeAddPlanWizard}>
            <Wizard.Header
              onClose={this.props.closeAddPlanWizard}
              title={this.state.wizardHeaderTitle}
            />
            <Wizard.Body>
              <Wizard.Steps
                steps={renderWizardSteps(
                  this.steps,
                  activeStepIndex,
                  activeSubStepIndex,
                  this.onStepClick
                )}
              />
              <Wizard.Row>
                <Wizard.Main>
                  {renderAddPlanWizardContents(this.steps)}
                </Wizard.Main>
              </Wizard.Row>
            </Wizard.Body>
            <Wizard.Footer>
              {activeStepIndex !== 4 && (
                <React.Fragment>
                  <Button
                    bsStyle="default"
                    className="btn-cancel"
                    onClick={this.props.closeAddPlanWizard}
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
                </React.Fragment>
              )}
              {activeStepIndex < 3 && (
                <Button
                  bsStyle="primary"
                  onClick={this.onNextButtonClick}
                  disabled={!this.isStepValid(activeStepIndex)}
                >
                  Next
                  <Icon type="fa" name="angle-right" />
                </Button>
              )}

              {activeStepIndex === 3 && (
                <Button bsStyle="primary" onClick={this.submitPlan}>
                  Submit Plan
                </Button>
              )}
              {activeStepIndex === 4 && (
                <Button
                  bsStyle="primary"
                  onClick={this.props.closeAddPlanWizard}
                >
                  Close
                </Button>
              )}
            </Wizard.Footer>
          </Wizard>
        </form>
      </div>
    );
  }
}

WizardAddPlan.propTypes = {
  kieServerId: PropTypes.string.isRequired,
  plan: PropTypes.object.isRequired,
  onPlanChanged: PropTypes.func.isRequired,
  onSavePlan: PropTypes.func.isRequired,
  closeAddPlanWizard: PropTypes.func.isRequired
};
