import React from "react";
import PropTypes from "prop-types";

export default class WizardBase extends React.Component {
  constructor(props, steps) {
    super(props);
    this.steps = steps;
  }
  onBackButtonClick = () => {
    const { activeStepIndex, activeSubStepIndex } = this.state;

    if (activeSubStepIndex > 0) {
      this.setState(prevState => ({
        activeSubStepIndex: prevState.activeSubStepIndex - 1
      }));
    } else if (activeStepIndex > 0) {
      this.setState(prevState => ({
        activeStepIndex: prevState.activeStepIndex - 1,
        activeSubStepIndex:
          this.steps[prevState.activeStepIndex - 1].subSteps.length - 1
      }));
    }
  };

  onNextButtonClick = () => {
    const { activeStepIndex, activeSubStepIndex } = this.state;
    const activeStep = this.steps[activeStepIndex];
    if (activeSubStepIndex < activeStep.subSteps.length - 1) {
      this.setState(prevState => ({
        activeSubStepIndex: prevState.activeSubStepIndex + 1
      }));
    } else if (activeStepIndex < this.steps.length - 1) {
      this.setState(prevState => ({
        activeStepIndex: prevState.activeStepIndex + 1,
        activeSubStepIndex: 0
      }));
    }
  };

  onStepClick = stepIndex => {
    if (stepIndex === this.state.activeStepIndex) {
      return;
    }
    this.setState({
      activeStepIndex: stepIndex,
      activeSubStepIndex: 0
    });
  };
  render() {
    return false;
  }
}

WizardBase.propTypes = {
  /** Initial step index */
  initialStepIndex: PropTypes.number,
  /** Initial sub step index */
  initialSubStepIndex: PropTypes.number
};

WizardBase.defaultProps = {
  initialStepIndex: 0,
  initialSubStepIndex: 0
};
