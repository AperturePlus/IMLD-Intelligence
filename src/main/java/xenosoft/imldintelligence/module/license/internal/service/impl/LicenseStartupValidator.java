package xenosoft.imldintelligence.module.license.internal.service.impl;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class LicenseStartupValidator implements ApplicationRunner {
    private static final String OPT_COMMAND = "license-cli-command";
    private final ValidateService validateService;

    public LicenseStartupValidator(ValidateService validateService) {
        this.validateService = validateService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (args.containsOption(OPT_COMMAND)) {
            return;
        }
        validateService.validateStartupOrThrow();
    }
}
