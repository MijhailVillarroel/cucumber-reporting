package net.masterthought.cucumber.generators.integrations;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import net.masterthought.cucumber.generators.TagReportPage;
import net.masterthought.cucumber.generators.integrations.helpers.BriefAssertion;
import net.masterthought.cucumber.generators.integrations.helpers.DocumentAssertion;
import net.masterthought.cucumber.generators.integrations.helpers.ElementAssertion;
import net.masterthought.cucumber.generators.integrations.helpers.TableRowAssertion;
import net.masterthought.cucumber.json.Step;
import net.masterthought.cucumber.json.support.TagObject;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class TagReportPageIntegrationTest extends PageTest {

    @Test
    public void generatePage_generatesTitle() {

        // given
        setUpWithJson(SAMPLE_JOSN);
        final TagObject tag = tags.get(0);
        page = new TagReportPage(reportResult, configuration, tag);
        final String titleValue = String.format("Cucumber-JVM Html Reports  - Tag: %s", tag.getName());

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
        final TagObject tag = tags.get(0);
        page = new TagReportPage(reportResult, configuration, tag);

        // when
        page.generatePage();

        // then
        DocumentAssertion document = documentFrom(page.getWebPage());
        TableRowAssertion bodyRow = document.getSummary().getTableStats().getBodyRow();

        bodyRow.hasExactValues(tag.getName(), "2", "1", "1", "16", "8", "1", "3", "2", "1", "1", "231ms", "Failed");
        bodyRow.hasExactCSSClasses("tagname", "", "", "", "", "", "failed", "skipped", "pending", "undefined", "missing", "duration", "failed");
    }

    @Test
    public void generatePage_generatesTagsList() {

        // given
        setUpWithJson(SAMPLE_JOSN);
        final TagObject tag = tags.get(0);
        page = new TagReportPage(reportResult, configuration, tag);

        // when
        page.generatePage();

        // then
        DocumentAssertion document = documentFrom(page.getWebPage());
        ElementAssertion[] elements = document.getElementsByTag();

        assertThat(elements).hasSameSizeAs(tags.get(0).getElements());
    }

    @Test
    public void generatePage_generatesSampleStep() {

        // given
        setUpWithJson(SAMPLE_JOSN);
        final TagObject tag = tags.get(1);
        page = new TagReportPage(reportResult, configuration, tag);

        // when
        page.generatePage();

        // then
        DocumentAssertion document = documentFrom(page.getWebPage());

        BriefAssertion stepElement = document.getElementsByTag()[0].getSteps().getSteps()[3].getBrief();
        Step step = tag.getElements().get(0).getSteps()[3];
        assertThat(stepElement.getName()).hasSameSizeAs(step.getName());
    }
}
