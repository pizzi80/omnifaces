# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build System
- **Full build**: `mvn clean compile` - Compiles Java and TypeScript, minifies JavaScript
- **Run tests**: `mvn clean verify` - Runs integration tests with WildFly-Mojarra (default profile)
- **Test profiles**: 
    - `mvn verify -P glassfish-mojarra`
    - `mvn verify -P tomcat-mojarra`
    - `mvn verify -P tomcat-myfaces`  
    - `mvn verify -P payara-mojarra`
- **Single test**: `mvn verify -Dtest=ClassName#methodName`
- **JavaScript build**: `npm run build` - Compiles TypeScript and bundles JavaScript
- **License format**: `mvn license:format`
- **Deploy snapshot**: `mvn clean deploy`
- **Release deploy**: `mvn clean deploy -P release`

### Browser Configuration
- **Change browser for tests**: `mvn clean verify -Darquillian.browser=firefox` (default: chrome)

## Architecture Overview

OmniFaces is a Jakarta Faces utility library with a three-tier architecture:

### Core Integration Layer
- **Initialization**: Three-phase startup (ServletContainerInitializer → ServletContextListener → SystemEventListener)
- **Wrapper Pattern**: `OmniApplication`, `OmniExternalContext`, `OmniPartialViewContext` extend standard Faces APIs
- **Factory Pattern**: Custom factories registered in faces-config.xml for seamless Faces integration

### Utility and Service Layer
- **Faces Utilities**: `org.omnifaces.util.Faces` - central facade for Faces API operations
- **Component Utils**: `Components` for traversal/manipulation, `Ajax` for programmatic control
- **CDI Integration**: Enhanced scopes (`@ViewScoped`), parameter injection (`@Param`), server push (`@Push`)

### Component and Resource Layer
- **Component Families**: Organized by function (`input`, `output`, `validator`, `script`, `tree`, etc.)
- **Resource Handlers**: Chain-of-responsibility for optimization (`CombinedResourceHandler`, `GraphicResourceHandler`, `PWAResourceHandler`)
- **Client Library**: TypeScript-based JavaScript library (`omnifaces.js`) for enhanced client behavior

### Key Integration Points
- **CDI Extensions** for custom scopes and injection
- **WebSocket Endpoints** for real-time server push
- **HTTP Filters** for request/response processing
- **Custom EL Resolvers** for enhanced expression language

## Project Structure

### Source Organization
- `src/main/java/org/omnifaces/` - Main Java source
    - `util/` - Core utility classes and service facades
    - `cdi/` - CDI integration (scopes, producers, extensions)
    - `component/` - Enhanced Faces components organized by family
    - `resourcehandler/` - Resource optimization and processing chain
    - `config/` - Configuration management and XML parsing
- `src/main/resources/META-INF/resources/omnifaces/omnifaces.js/` - TypeScript source files
- `src/test/` - Arquillian-based integration tests with multiple server profiles

### Build Artifacts
- TypeScript compiles to `target/tsc/omnifaces.unminified.js`
- Minified to `META-INF/resources/omnifaces/omnifaces.js` in final JAR
- Service worker minified from `sw.unminified.js` to `sw.js`

## Testing Framework

Uses Arquillian for integration testing across multiple Jakarta Faces implementations and servers. Each test creates a minimal WAR deployment with OmniFaces and required dependencies, then runs browser-based tests using Selenium WebDriver.

### Test Profiles
- **wildfly-mojarra** (default): WildFly with Mojarra Faces implementation  
- **glassfish-mojarra**: GlassFish with Mojarra
- **tomcat-mojarra/tomcat-myfaces**: Tomcat with respective Faces implementations
- **payara-mojarra**: Payara with Mojarra

Test classes follow pattern `*IT.java` and are located in `src/test/java/org/omnifaces/test/`.