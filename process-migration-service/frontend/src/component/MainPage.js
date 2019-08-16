import React, { Component } from "react";
import { MenuItem, Icon } from "patternfly-react";

import MigrationPlans from "./tabMigrationPlan/MigrationPlans";
import MigrationDefinitions from "./tabMigration/MigrationDefinitions";
import KieServerClient from "../clients/kieServerClient";
import { ALERT_TYPE_ERROR } from "patternfly-react/dist/js/components/Alert/AlertConstants";
import Notification from "./Notification";
import {
  Masthead,
  MastheadDropdown
} from "patternfly-react/dist/js/components/Masthead";
import MastheadCollapse from "patternfly-react/dist/js/components/Masthead/MastheadCollapse";
import {
  HorizontalNavMenu,
  HorizontalNav,
  HorizontalNavMenuItem
} from "patternfly-react/dist/js/components/HorizontalNav";

export default class MainPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      kieServerId: "",
      title: "KIE Server Name",
      menuItems: [],
      errorMsg: "",
      showMigrations: false,
      showPlans: true
    };
  }

  componentDidMount() {
    this.loadKieServers();
  }

  showMigrations = () => {
    this.setState({
      showMigrations: true,
      showPlans: false
    });
  };

  showPlans = () => {
    this.setState({
      showMigrations: false,
      showPlans: true
    });
  };

  setErrorMsg = errorMsg => this.setState({ errorMsg });

  resetErrorMsg = () => this.setErrorMsg("");

  loadKieServers = () => {
    KieServerClient.getKieServers()
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
    menuItems.push(<MenuItem key="-" divider />);
    menuItems.push(
      <MenuItem
        key="refresh"
        title="Refresh servers list"
        onSelect={this.loadKieServers}
      >
        <Icon type="fa" name="refresh" />
        &nbsp; Refresh
      </MenuItem>
    );
    this.setState({ menuItems });
  };

  handleChange = option => {
    this.setState({ kieServerId: option });
  };

  render() {
    const notification = (
      <Notification
        type={ALERT_TYPE_ERROR}
        message={this.state.errorMsg}
        onDismiss={() => this.setState({ errorMsg: "" })}
      />
    );
    return (
      <React.Fragment>
        <HorizontalNav>
          <Masthead title="Process instance migration" thin navToggle>
            <MastheadCollapse>
              <MastheadDropdown
                id="kieservers-dropdown"
                title={
                  <span>
                    <Icon type="fa" name="server" />
                    &nbsp;
                    {this.state.kieServerId}
                  </span>
                }
              >
                {this.state.menuItems}
              </MastheadDropdown>
            </MastheadCollapse>
          </Masthead>
          {this.state.errorMsg && notification}
          <HorizontalNavMenu>
            <HorizontalNavMenuItem
              id="plans"
              title="Migration plans"
              onItemClick={this.showPlans}
            />
            <HorizontalNavMenuItem
              id="migrations"
              title="Migrations"
              onItemClick={this.showMigrations}
            />
          </HorizontalNavMenu>
        </HorizontalNav>
        <div id="pim-content">
          {this.state.showPlans && (
            <MigrationPlans
              kieServerId={this.state.kieServerId}
              onError={this.setErrorMsg}
            />
          )}
          {this.state.showMigrations && (
            <MigrationDefinitions onError={this.setErrorMsg} />
          )}
        </div>
      </React.Fragment>
    );
  }
}
