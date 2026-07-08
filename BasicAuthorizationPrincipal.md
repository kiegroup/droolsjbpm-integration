# `BasicAuthorizationPrincipal` — Analysis

## Definition

**File:** [`kie-tomcat-integration/src/main/java/org/kie/integration/tomcat/BasicAuthorizationPrincipal.java`](kie-tomcat-integration/src/main/java/org/kie/integration/tomcat/BasicAuthorizationPrincipal.java)

A minimal `java.security.Principal` implementation that holds a **Base64-encoded HTTP Basic Authorization header string** (e.g. `"Basic dXNlcjpwYXNzd29yZA=="`). It has a single `String name` field and only overrides `getName()`.

Its purpose is to **carry the raw Basic Auth credentials** as a `Principal` object into a JAAS/JACC `Subject`, so that downstream components (specifically the KIE Server) can extract or identify it later.

---

## Usage Sites

All usages are confined to the `droolsjbpm-integration` repository. No other project in the workspace references this class.

---

### 1. `TomcatRealmLoginModule` — creation during JAAS login

**File:** [`kie-tomcat-integration/src/main/java/org/kie/integration/tomcat/TomcatRealmLoginModule.java`](kie-tomcat-integration/src/main/java/org/kie/integration/tomcat/TomcatRealmLoginModule.java) — line 75

```java
principal = applicationRealm.authenticate(name, password);
if (principal != null) {
    String basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString((name + ":" + password).getBytes("UTF-8"));
    clientPrincipal = new BasicAuthorizationPrincipal(basicAuthHeader);
    return true;
}
```

Then in `commit()` (line 109), the `clientPrincipal` is added to the `Subject`'s principal set:

```java
subject.getPrincipals().add(clientPrincipal);
```

**When:** During the JAAS login commit phase for Tomcat. After Tomcat's realm authenticates the user (`name` + `password`), a `BasicAuthorizationPrincipal` is constructed and stored alongside the role/user principals in the `Subject`. This enables KIE Server components consuming that `Subject` to later reconstruct or recognize the Basic Auth token.

---

### 2. `JACCValve` — creation during JACC policy context setup

**File:** [`kie-tomcat-integration/src/main/java/org/kie/integration/tomcat/JACCValve.java`](kie-tomcat-integration/src/main/java/org/kie/integration/tomcat/JACCValve.java) — line 93

```java
if (req.getPrincipal() instanceof GenericPrincipal genericPrincipal) {
    String name = genericPrincipal.getName();
    // Not sure if the actual password and full basicAuthHeader is really needed
    // Removing password for the moment being
    //String password = genericPrincipal.getPassword();
    String password = "";
    String basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString((name + ":" + password).getBytes(StandardCharsets.UTF_8));
    principals.add(new BasicAuthorizationPrincipal(basicAuthHeader));
}
```

This is in the JACC `PolicyContextHandler` callback that builds a `Subject` from the current Tomcat `Request`. The `BasicAuthorizationPrincipal` is added to the set of principals (alongside the user principal and group principal) to form the `Subject` registered with the JACC `PolicyContext`.

> **Note:** The password is explicitly left empty (`""`) — a deliberate security decision noted in the code comment.

---

### 3. `JACCIdentityProvider` — exclusion filter when resolving the username

**File:** [`kie-server-parent/kie-server-services/kie-server-services-common/src/main/java/org/kie/server/services/impl/security/JACCIdentityProvider.java`](kie-server-parent/kie-server-services/kie-server-services-common/src/main/java/org/kie/server/services/impl/security/JACCIdentityProvider.java) — line 123

```java
protected boolean supportedPrincipal(Principal principal) {
    return !isGroupPrincipal(principal)
        && !principal.getClass().getName().endsWith("BasicAuthorizationPrincipal");
}
```

This method is called inside `getName()` to iterate the `Subject`'s principals and return the **user's actual name**:

```java
for (Principal principal : principals) {
    if (supportedPrincipal(principal)) {
        return principal.getName();   // returns the username, not the auth header
    }
}
```

Because a `BasicAuthorizationPrincipal`'s `getName()` returns the raw Base64 header string rather than a human-readable username, it is explicitly **excluded** from name resolution.

> **Note:** The check uses a class-name suffix string comparison (`endsWith("BasicAuthorizationPrincipal")`) rather than `instanceof`, because `JACCIdentityProvider` lives in a separate module (`kie-server-services-common`) and does not have a compile-time dependency on the `kie-tomcat-integration` class.

---

## Summary

| File | Purpose |
|---|---|
| [`BasicAuthorizationPrincipal.java`](kie-tomcat-integration/src/main/java/org/kie/integration/tomcat/BasicAuthorizationPrincipal.java) | Class definition — a thin `Principal` wrapping a Base64 Basic Auth header string |
| [`TomcatRealmLoginModule.java`](kie-tomcat-integration/src/main/java/org/kie/integration/tomcat/TomcatRealmLoginModule.java) | **Creates** an instance after successful JAAS authentication; commits it into the `Subject` alongside user/role principals |
| [`JACCValve.java`](kie-tomcat-integration/src/main/java/org/kie/integration/tomcat/JACCValve.java) | **Creates** an instance inside the JACC `PolicyContextHandler`; adds it to the `Subject` registered with the JACC context (password intentionally blank) |
| [`JACCIdentityProvider.java`](kie-server-parent/kie-server-services/kie-server-services-common/src/main/java/org/kie/server/services/impl/security/JACCIdentityProvider.java) | **Filters it out** when resolving the current user's name from the `Subject`, to avoid returning the raw auth header as the username |
