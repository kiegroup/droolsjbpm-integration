import React from "react";
import PropTypes from "prop-types";

import { Wizard, Button, Icon } from "patternfly-react";

import MigrationClient from "../../../clients/migrationClient";
import { ExecuteMigrationItems } from "../../common/WizardItems";
import PageMigrationRunningInstances from "./PageMigrationRunningInstances";
import PageReview from "../PageReview";
import { renderWizardSteps } from "../PfWizardRenderers";
import WizardBase from "../WizardBase";

import PageMigrationScheduler from "./PageMigrationScheduler";
import kieServerClient from "../../../clients/kieServerClient";

export default class WizardExecuteMigration extends WizardBase {
  constructor(props) {
    super(props, ExecuteMigrationItems);
    this.state = {
      activeStepIndex: 0,
      activeSubStepIndex: 0,
      scheduledStartTime: "",
      callbackUrl: "",
      definition: {
        execution: {
          type: "ASYNC"
        }
      },
      stepValidation: {}
    };
    kieServerClient
      .getInstances(this.props.kieServerId, this.props.containerId)
      .then(runningInstances => this.setState({ runningInstances }))
      .catch(() => {
        this.setState({
          runningInstances: []
        });
      });
  }

  onSubmitMigrationPlan = () => {
    MigrationClient.create(this.state.migration).then(migration => {
      this.setState({ migration });
      this.onNextButtonClick();
    });
  };

  setRunngingInstancesIds = ids => {
    const { definition } = this.state;
    definition.processInstanceIds = ids;
    this.setState({ definition });
  };

  onExecutionFieldChange = (field, value) => {
    const { definition } = this.state;
    definition.execution[field] = value;
    this.setState({ definition });
  };

  setStepIsValid = (step, isValid) => {
    const { stepValidation } = this.state;
    stepValidation[step] = isValid;
    this.setState({ stepValidation });
  };

  isStepValid = step => {
    return this.state.stepValidation[step];
  };

  render() {
    const { activeStepIndex, activeSubStepIndex } = this.state;

    const renderExecuteMigrationWizardContents = wizardSteps => {
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
                {this.state.runningInstances && (
                  <PageMigrationRunningInstances
                    runningInstances={this.state.runningInstances}
                    setRunngingInstancesIds={this.setRunngingInstancesIds}
                    onIsValid={isValid => this.setStepIsValid(0, isValid)}
                  />
                )}
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
                <PageMigrationScheduler
                  callbackUrl={this.state.definition.execution.callbackUrl}
                  scheduledStartTime={
                    this.state.definition.execution.scheduledStartTime
                  }
                  onFieldChange={this.onExecutionFieldChange}
                  onIsValid={isValid => this.setStepIsValid(1, isValid)}
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
                  object={this.state.definition}
                  exportedFileName="migration"
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
                  object={this.state.definition}
                  exportedFileName="migration"
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
                  this.steps,
                  activeStepIndex,
                  activeSubStepIndex,
                  this.onStepClick
                )}
              />
              <Wizard.Row>
                <Wizard.Main>
                  {renderExecuteMigrationWizardContents(
                    this.steps,
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
                  disabled={!this.isStepValid(activeStepIndex)}
                  onClick={this.onNextButtonClick}
                >
                  Next
                  <Icon type="fa" name="angle-right" />
                </Button>
              )}
              {activeStepIndex === 2 && (
                <Button bsStyle="primary" onClick={this.onSubmitMigrationPlan}>
                  Execute Plan
                </Button>
              )}
              {activeStepIndex === 3 && (
                <Button
                  bsStyle="primary"
                  onClick={this.props.closeMigrationWizard}
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

WizardExecuteMigration.propTypes = {
  kieServerId: PropTypes.string.isRequired,
  containerId: PropTypes.string.isRequired
};
