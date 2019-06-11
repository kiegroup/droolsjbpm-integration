import React, { Component } from "react";
import classNames from "classnames";
import {
  TabContainer,
  Nav,
  NavItem,
  TabPane,
  TabContent,
  DropdownButton,
  MenuItem,
  Button,
  Icon,
  ALERT_TYPE_ERROR
} from "patternfly-react";

import MigrationPlans from "./tabMigrationPlan/MigrationPlans";
import MigrationDefinitions from "./tabMigration/MigrationDefinitions";
import { BACKEND_URL } from "./common/PimConstants";
import Notification from "./Notification";

export default class MainPageWithPfTab extends Component {
  constructor(props) {
    super(props);

    this.state = {
      kieServerId: "",
      title: "KIE Server Name",
      menuItems: []
    };
  }

  componentDidMount() {
    this.loadKieServers();
  }

  setErrorMsg = errorMsg => this.setState({ errorMsg });

  resetErrorMsg = () => this.setErrorMsg("");

  loadKieServers = () => {
    this.resetErrorMsg();
    fetch(`${BACKEND_URL}/kieserver`, {
      headers: {
        "Content-Type": "application/json"
      },
      credentials: "same-origin"
    })
      .then(res => {
        if (!res.ok) {
          throw res;
        }
        return res.json();
      })
      .then(res => this.populateKieServers(res))
      .catch(error => {
        this.populateKieServers([]);
        this.setErrorMsg(error);
      });
  };

  populateKieServers = kieServers => {
    let menuItems = [];
    if (kieServers.size === 0) {
      menuItems.push(
        <MenuItem key={0} eventKey="no-kieservers" disabled>
          No configured KieServers
        </MenuItem>
      );
    } else {
      kieServers.map((kieServer, i) => {
        const kieServerId =
          kieServer.id === null ? "unknown-" + i : kieServer.id;
        if (this.state.kieServerId === "") {
          this.setState({ kieServerId });
        }
        menuItems.push(
          <MenuItem
            key={i}
            eventKey={kieServerId}
            onSelect={this.handleChange}
            disabled={kieServer.status !== "SUCCESS"}
          >
            {kieServerId}
          </MenuItem>
        );
      });
    }
    this.setState({ menuItems });
  };

  handleChange = option => {
    this.setState({ kieServerId: option });
  };

  render() {
    const bsClass = classNames("nav nav-tabs nav-tabs-pf", {
      "nav-justified": false
    });
    const notification = (
      <Notification
        type={ALERT_TYPE_ERROR}
        message={this.state.errorMsg}
        onDismiss={() => this.setState({ errorMsg: "" })}
      />
    );
    return (
      <div className="">
        {this.state.errorMsg && notification}
        <div className="row">
          <div className="col-xs-9">
            <h1>Process Instance Migration</h1>
          </div>
          <div className="col-xs-3">
            <br />
            <div className="pull-right">
              <span>KIE Server: </span>
              <DropdownButton id={"kieDropDown"} title={this.state.kieServerId}>
                {this.state.menuItems}
              </DropdownButton>
              <Button onClick={this.loadKieServers}>
                <Icon type="fa" name="refresh" />
              </Button>
            </div>
          </div>
        </div>
        <TabContainer id="tabs-with-dropdown-pf" defaultActiveKey="first">
          <div>
            <Nav bsClass={bsClass}>
              <NavItem eventKey="first">Migration Plans</NavItem>
              <NavItem eventKey="second">Migrations</NavItem>
            </Nav>

            <TabContent animation>
              <TabPane eventKey="first">
                <MigrationPlans
                  kieServerId={this.state.kieServerId}
                  onError={this.setErrorMsg}
                />
              </TabPane>
              <TabPane eventKey="second">
                <MigrationDefinitions onError={this.setErrorMsg} />
              </TabPane>
            </TabContent>
          </div>
        </TabContainer>
      </div>
    );
  }
}
