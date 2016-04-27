package net.masterthought.cucumber.generators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import net.masterthought.cucumber.generators.helpers.BriefAssertion;
import net.masterthought.cucumber.generators.helpers.DocumentAssertion;
import net.masterthought.cucumber.generators.helpers.ElementAssertion;
import net.masterthought.cucumber.generators.helpers.FeatureAssertion;
import net.masterthought.cucumber.generators.helpers.HookAssertion;
import net.masterthought.cucumber.generators.helpers.StepAssertion;
import net.masterthought.cucumber.generators.helpers.TableAssertion;
import net.masterthought.cucumber.generators.helpers.TableRowAssertion;
import net.masterthought.cucumber.generators.helpers.TagAssertion;
import net.masterthought.cucumber.json.Element;
import net.masterthought.cucumber.json.Feature;
import net.masterthought.cucumber.json.Hook;
import net.masterthought.cucumber.json.Row;
import net.masterthought.cucumber.json.Step;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class FeatureReportPageIntegrationTest extends Page {

    @Test
    public void generatePage_generatesTitle() {

        // given
        setUpWithJson(SAMPLE_JOSN);
        final Feature feature = features.get(0);
        page = new FeatureReportPage(reportResult, configuration, feature);
        final String titleValue = String.format("Cucumber-JVM Html Reports  - Feature: %s", feature.getName());

        // when
        page.generatePage();

        // then
        DocumentAssertion document = documentFrom(page.getWebPage());
        String title = document.getHead().getTitle();

        assertThat(title).isEqualTo(titleValue);
    }

    @Test
    public void generatePage_generatesStatsTableBody() {

        // given
        setUpWithJson(SAMPLE_JOSN);
        configuration.setStatusFlags(true, false, false, true);
        final Feature feature = features.get(0);
        page = new FeatureReportPage(reportResult, configuration, feature);

        // when
        page.generatePage();

        // then
        DocumentAssertion document = documentFrom(page.getWebPage());
        TableRowAssertion odyRow = document.getSummary().getTableStats().getBodyRow();

        odyRow.hasExactValues(feature.getName(), "1", "1", "0", "10", "7", "0", "0", "2", "1", "0", "343 ms", "Passed");
        odyRow.hasExactCSSClasses("tagname", "", "", "", "", "", "", "", "pending", "undefined", "", "duration", "passed");
    }

    @Test
    public void generatePage_generatesFeatureDetails() {

        // given
        setUpWithJson(SAMPLE_JOSN);
        final Feature feature = features.get(0);
        page = new FeatureReportPage(reportResult, configuration, feature);

        // when
        page.generatePage();

        // then
        DocumentAssertion document = documentFrom(page.getWebPage());
        FeatureAssertion featureDetails = document.getFeature();

        BriefAssertion brief = featureDetails.getBrief();
        assertThat(brief.getKeyword()).isEqualTo(feature.getKeyword());
        assertThat(brief.getName()).isEqualTo(feature.getName());
        brief.hasStatus(feature.getStatus());

        TagAssertion[] tags = featureDetails.getTags();
        assertThat(tags).hasSize(1);
        tags[0].getLink().hasLabelAndAddress("@featureTag", "featureTag.html");

        assertThat(featureDetails.getDescription()).isEqualTo(feature.getDescription());
    }

    @Test
    public void generatePage_generatesScenarioDetails() {

        // given
        setUpWithJson(SAMPLE_JOSN);
        final Feature feature = features.get(0);
        page = new FeatureReportPage(reportResult, configuration, feature);

        // when
        page.generatePage();

        // then
        DocumentAssertion document = documentFrom(page.getWebPage());

        ElementAssertion[] elements = document.getFeature().getElements();
        assertThat(elements).hasSize(feature.getElements().length);

        ElementAssertion firstElement = elements[1];
        Element scenario = feature.getElements()[1];

        TagAssertion[] tags = firstElement.getTags();
        assertThat(tags).hasSize(scenario.getTags().length);
        for (int i = 0; i < tags.length; i++) {
            tags[i].getLink().hasLabelAndAddress(scenario.getTags()[i].getName(), scenario.getTags()[i].getFileName());
        }

        BriefAssertion brief = firstElement.getBrief();
        assertThat(brief.getKeyword()).isEqualTo(scenario.getKeyword());
        assertThat(brief.getName()).isEqualTo(scenario.getName());
        brief.hasStatus(scenario.getStatus());

        assertThat(firstElement.getDescription()).isEqualTo(scenario.getDescription());
    }

    @Test
    public void generatePage_generatesHooks() {

        // given
        setUpWithJson(SAMPLE_JOSN);
        final Feature feature = features.get(1);
        page = new FeatureReportPage(reportResult, configuration, feature);

        // when
        page.generatePage();

        // then
        DocumentAssertion document = documentFrom(page.getWebPage());

        ElementAssertion secondElement = document.getFeature().getElements()[0];

        Element element = feature.getElements()[0];

        HookAssertion[] before = secondElement.getBefore();
        assertThat(before).hasSize(element.getBefore().length);
        validateHook(before, element.getBefore(), "Before");

        HookAssertion[] after = secondElement.getAfter();
        assertThat(after).hasSize(element.getAfter().length);
        validateHook(after, element.getAfter(), "After");
    }

    @Test
    public void generatePage_generatesSteps() {

        // given
        setUpWithJson(SAMPLE_JOSN);
        final Feature feature = features.get(1);
        page = new FeatureReportPage(reportResult, configuration, feature);

        // when
        page.generatePage();

        // then
        DocumentAssertion document = documentFrom(page.getWebPage());

        ElementAssertion secondElement = document.getFeature().getElements()[0];
        Element element = feature.getElements()[0];

        StepAssertion[] steps = secondElement.getSteps();
        assertThat(steps).hasSameSizeAs(element.getSteps());

        for (int i = 0; i < steps.length; i++) {
            BriefAssertion brief = steps[i].getBrief();
            Step step = element.getSteps()[i];

            brief.hasStatus(step.getStatus());
            assertThat(brief.getKeyword()).isEqualTo(step.getKeyword());
            assertThat(brief.getName()).isEqualTo(step.getName());
            brief.hasDuration(step.getDuration());
        }
    }

    @Test
    public void generatePage_generatesArguments() {

        // given
        setUpWithJson(SAMPLE_JOSN);
        final Feature feature = features.get(0);
        page = new FeatureReportPage(reportResult, configuration, feature);

        // when
        page.generatePage();

        // then
        DocumentAssertion document = documentFrom(page.getWebPage());

        StepAssertion stepElement = document.getFeature().getElements()[0].getSteps()[1];
        TableAssertion argTable = stepElement.getArgumentsTable();

        Step step = feature.getElements()[0].getSteps()[1];

        for (int r = 0; r < step.getRows().length; r++) {
            Row row = step.getRows()[r];
            TableRowAssertion rowElement = argTable.getBodyRows()[r];

            assertThat(rowElement.getCellsValues()).isEqualTo(row.getCells());
        }
    }

    private static void validateHook(HookAssertion[] elements, Hook[] hooks, String hookName) {
        for (int i = 0; i < elements.length; i++) {
            BriefAssertion brief = elements[i].getBrief();
            assertThat(brief.getKeyword()).isEqualTo(hookName);
            brief.hasStatus(hooks[i].getStatus());

            if (hooks[i].getMatch() != null) {
                assertThat(brief.getName()).isEqualTo(hooks[i].getMatch().getLocation());
            }
            if (hooks[i].getResult() != null) {
                brief.hasDuration(hooks[i].getResult().getDuration());
                if (hooks[i].getResult().getErrorMessage() != null) {
                    assertThat(elements[i].getErrorMessage()).contains(hooks[i].getResult().getErrorMessage());
                }
            }
        }
    }

}
