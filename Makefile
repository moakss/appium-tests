# Test runs for Android / iOS. The tests start their own Appium server, or reuse one already running
# (e.g. `make appium`), see AppiumServerManager.
#
#   make smoke PLATFORM=ios
#   make test TEST=CartTest                            one class
#   make test TEST='CartTest#shouldShowEmptyCartAndGoShopping,CatalogTest'
#   make tc TC=TC-04                                   test case ID(s) from docs/TEST_CASES.md
#   make tc TC='TC-10 TC-12' PLATFORM=ios
#   make smoke APP=~/builds/app-debug.apk               install this build instead of using the installed app

PLATFORM ?= android
SUITE ?= smoke
TEST ?=
TC ?=
APP ?=

MVN ?= mvn
# Optional app build to install; without it the app already installed on the device is used.
MVN_OPTS = -Dplatform=$(PLATFORM) $(if $(APP),-Dapp.path='$(APP)')
TEST_SOURCES := $(shell find src/test/java/com/mydemoappappium/tests -name '*Test.java')

# Prints Class#method for every test method whose description starts with one of the IDs in $(TC).
define tests_for_tc
awk -v ids="$(TC)" ' \
  BEGIN { n = split(ids, want, " ") } \
  /description *= *"TC-/ { hit = 0; for (i = 1; i <= n; i++) if (index($$0, "\"" want[i] " ")) hit = 1 } \
  hit && /public void/ { match($$0, /void [A-Za-z0-9_]+/); f = FILENAME; sub(/.*\//, "", f); sub(/\.java$$/, "", f); \
                         print f "#" substr($$0, RSTART + 5, RLENGTH - 5); hit = 0 }' $(TEST_SOURCES) | paste -sd, -
endef

.PHONY: help test smoke regression tc list appium report clean

help: ## Show targets and variables
	@grep -E '^[a-z-]+:.*## ' $(MAKEFILE_LIST) | awk -F ':.*## ' '{ printf "  %-12s %s\n", $$1, $$2 }'
	@echo
	@echo "  Variables: PLATFORM=android|ios (default $(PLATFORM)), SUITE=smoke|regression,"
	@echo "             TEST=Class[#method][,...], TC='TC-04 TC-10', APP=/path/to/app.apk|.app (optional)"

test: ## Run SUITE, or only TEST when given
	$(MVN) test $(MVN_OPTS) -Dsuite=$(SUITE) $(if $(TEST),-Dtest='$(TEST)')

smoke: ## Run the smoke suite
	$(MAKE) test SUITE=smoke TEST=

regression: ## Run the regression suite
	$(MAKE) test SUITE=regression TEST=

tc: ## Run the tests of test case ID(s) TC
	@test -n "$(TC)" || { echo "Set TC, e.g. make tc TC=TC-04" >&2; exit 1; }
	@tests="$$($(tests_for_tc))"; \
	test -n "$$tests" || { echo "No test found for $(TC); see make list" >&2; exit 1; }; \
	echo "Running $$tests"; \
	$(MVN) test $(MVN_OPTS) -Dsuite=regression -Dtest="$$tests"

list: ## List test case IDs with their Class#method
	@awk ' \
	  /description *= *"TC-/ { match($$0, /"TC-[^"]*"/); desc = substr($$0, RSTART + 1, RLENGTH - 2) } \
	  desc != "" && /public void/ { match($$0, /void [A-Za-z0-9_]+/); f = FILENAME; sub(/.*\//, "", f); sub(/\.java$$/, "", f); \
	                                printf "  %-75s %s#%s\n", desc, f, substr($$0, RSTART + 5, RLENGTH - 5); desc = "" }' \
	  $(TEST_SOURCES) | sort

appium: ## Start an Appium server with Inspector for manual use (tests reuse it)
	./scripts/start-appium.sh

report: ## Open the Allure report of the last run
	$(MVN) allure:serve

clean: ## Remove build output, reports and failure artifacts
	$(MVN) clean
