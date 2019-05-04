import React from "react";
import axios from "axios";

import {
  MockupData_planList,
  MockupData_runningInstances,
  MockupData_PIM_response
} from "../common/MockupData";
import { BACKEND_URL, USE_MOCK_DATA } from "../common/PimConstants";

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
    if (USE_MOCK_DATA) {
      const plans = MockupData_planList;
      this.setState({ plans, filteredPlans: plans });
    } else {
      const servicesUrl = BACKEND_URL + "/plans";
      axios.get(servicesUrl, {}).then(res => {
        const plans = res.data;
        this.setState({ plans, filteredPlans: plans });
      });
    }
  };

  showDeleteDialog = id => {
    this.setState({
      showDeleteConfirmation: true,
      deletePlanId: id
    });
  };

  hideDeleteDialog = () => {
    this.setState({
      showDeleteConfirmation: false
    });
  };

  deletePlan = () => {
    if (USE_MOCK_DATA) {
      this.retrieveAllPlans();
      this.hideDeleteDialog();
    } else {
      //need to create a temp variable "self" to store this, so I can invoke this inside axios call
      const self = this;
      const serviceUrl = BACKEND_URL + "/plans/" + this.state.deletePlanId;
      axios
        .delete(serviceUrl, {
          headers: {
            "Content-Type": "application/json"
          }
        })
        .then(function() {
          self.retrieveAllPlans();
          self.hideDeleteDialog();
        });
    }
  };

  // addPlan need to be in the parent because it's shared between WizardAddPlan and Import Plan pop-up
  addPlan = plan => {
    if (USE_MOCK_DATA) {
      this.setState({
        addPlanResponseJsonStr: JSON.stringify(MockupData_PIM_response, null, 2)
      });
      this.retrieveAllPlans();
    } else {
      if (plan !== null && plan !== "") {
        //step 1, replace all \" to "
        plan = plan.replace(/\\"/g, '"');
        //step 2, replace "{ to {
        plan = plan.replace('"{', "{");
        //step3, replace }" to }
        plan = plan.replace('}"', "}");
      }

      //need to create a temp variable "self" to store this, so I can invoke this inside axios call
      const self = this;
      const servicsUrl = BACKEND_URL + "/plans";
      axios
        .post(servicsUrl, plan, {
          headers: {
            "Content-Type": "application/json"
          }
        })
        .then(function(response) {
          self.setState({
            addPlanResponseJsonStr: JSON.stringify(response.data, null, 2)
          });
          self.retrieveAllPlans();
        });
    }
  };

  editPlan = (plan, planId) => {
    if (USE_MOCK_DATA) {
      this.retrieveAllPlans();
    } else {
      //need to create a temp variable "self" to store this, so I can invoke this inside axios call
      const self = this;
      const serviceUrl = BACKEND_URL + "/plans/" + planId;
      axios
        .put(serviceUrl, plan, {
          headers: {
            "Content-Type": "application/json"
          }
        })
        .then(function(response) {
          self.setState({
            addPlanResponseJsonStr: JSON.stringify(response.data, null, 2)
          });
          self.retrieveAllPlans();
        });
    }
  };

  openMigrationWizard = rowData => {
    if (USE_MOCK_DATA) {
      const instances = MockupData_runningInstances;

      this.setState({
        runningInstances: instances,
        showMigrationWizard: true,
        planId: rowData.id
      });
      this.refs.WizardExecuteMigrationChild.resetWizardStates();
    } else {
      const servicesUrl = BACKEND_URL + "/kieserver/instances";
      axios
        .get(servicesUrl, {
          params: {
            containerId: rowData.sourceContainerId,
            kieServerId: this.props.kieServerIds
          }
        })
        .then(res => {
          const instances = res.data;
          this.setState({
            runningInstances: instances,
            showMigrationWizard: true,
            planId: rowData.id
          });
          this.refs.WizardExecuteMigrationChild.resetWizardStates();
        });
    }
  };
}
