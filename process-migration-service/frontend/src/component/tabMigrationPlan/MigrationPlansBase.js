import React from "react";
import KieServerClient from "../../clients/kieServerClient";
import PlanClient from "../../clients/planClient";

export default class MigrationPlansBase extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      plans: [],
      filteredPlans: [],
      showDeleteConfirmation: false,
      showMigrationWizard: false,
      showPlanWizard: false,
      deletePlanId: "",
      runningInstances: [],
      addPlanResponseJsonStr: ""
    };
  }

  componentDidMount() {
    this.retrieveAllPlans();
  }

  retrieveAllPlans = () => {
    PlanClient.getAll().then(plans => {
      this.setState({ plans, filteredPlans: plans });
    });
  };

  savePlan = async () => {
    return this.persistPlan(this.state.plan).then(plan => {
      this.setState({ plan });
      this.retrieveAllPlans();
    });
  };

  importPlan = async plan => {
    return this.persistPlan(plan).then(() => this.retrieveAllPlans());
  };

  persistPlan = async plan => {
    if (plan.id === undefined) {
      return PlanClient.create(plan);
    }
    return PlanClient.update(plan.id, plan);
  };

  onPlanUpdated = plan => {
    this.setState({ plan });
  };

  openMigrationWizard = rowData => {
    KieServerClient.getInstances(this.props.kieServerId)
      .then(res => {
        const instances = res.data;
        this.setState({
          runningInstances: instances,
          showMigrationWizard: true,
          plan: rowData,
          errorMsg: ""
        });
        this.refs.WizardExecuteMigrationChild.resetWizardStates();
      })
      .catch(error => {
        this.setState({ errorMsg: error.message });
      });
  };
}
