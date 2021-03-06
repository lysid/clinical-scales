/*
 * Copyright (C) 2017 Frederic Monjo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package studio.lysid.scales.query.indicator;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import studio.lysid.scales.query.scale.ScaleAggregate;
import studio.lysid.scales.query.scale.ScaleId;
import studio.lysid.scales.query.scale.ScaleStatus;
import studio.lysid.scales.query.scale.ScaleSteps;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class IndicatorSteps {

    private Exception thrownException;

    private IndicatorAggregate someIndicator;
    private IndicatorAggregate anotherIndicator;

    private ScaleAggregate someScale;
    private ScaleAggregate anotherScale;



    @Before()
    public void prepareForNewScenario() {
        this.thrownException = null;
        this.someIndicator = null;
        this.anotherIndicator = null;
        this.someScale = null;
        this.anotherScale = null;
    }



    @Given("^(?:a|an) (Draft|Published|Archived|Evolved) Indicator$")
    public void aStatusIndicator(String statusName) {
        this.someIndicator = createIndicatorWithStatus(new IndicatorId(UUID.fromString("someIndicator")), IndicatorStatus.valueOf(statusName));
    }

    private IndicatorAggregate createIndicatorWithStatus(IndicatorId id, IndicatorStatus status) {
        IndicatorAggregate newIndicator = new IndicatorAggregate(id);
        if (status != null) {
            switch (status) {
                case Draft:
                    // Nothing to do
                    break;

                case Published:
                    newIndicator.publish();
                    break;

                case Archived:
                    newIndicator.archive(null);
                    break;

                case Evolved:
                    newIndicator.publish();
                    newIndicator.evolveInto(new IndicatorId(UUID.fromString("someEvolvingIndicator")));
                    break;
            }
        }
        return newIndicator;
    }

    @When("^I create a new Indicator$")
    public void iCreateANewIndicator() {
        this.someIndicator = createIndicatorWithStatus(new IndicatorId(UUID.fromString("someIndicator")), null);
    }

    @When("^I publish this Indicator$")
    public void iPublishThisIndicator() {
        try {
            this.someIndicator.publish();
        } catch (Exception e) {
            this.thrownException = e;
        }
    }

    @When("^I archive this Indicator$")
    public void iArchiveThisIndicator() {

        List<ScaleAggregate> scalesUsingThisIndicator = null;
        if (this.someScale != null) {
            if (this.anotherScale != null) {
                scalesUsingThisIndicator = Arrays.asList(this.someScale, this.anotherScale);
            } else {
                scalesUsingThisIndicator = Collections.singletonList(this.someScale);
            }
        }

        try {
            this.someIndicator.archive(scalesUsingThisIndicator);
        } catch (Exception e) {
            this.thrownException = e;
        }
    }

    @When("^I unarchive this Indicator$")
    public void iUnarchiveThisIndicator() {
        try {
            this.someIndicator.unarchive();
        } catch (Exception e) {
            this.thrownException = e;
        }
    }

    @Then("^(?:its|the Indicator) status should be (Draft|Published|Archived|Evolved)$")
    public void itsStatusShouldBe(String statusName) {
        assertNull(this.thrownException);
        assertEquals(this.someIndicator.getStatus(), IndicatorStatus.valueOf(statusName));
    }

    @Then("^it should fail with message \"([^\"]*)\"$")
    public void itShouldThrowAnIllegalStateExceptionWithMessage(String message) {
        assertNotNull(this.thrownException);
        assertEquals(this.thrownException.getClass(), IllegalStateException.class);
        assertEquals(this.thrownException.getMessage(), message);
    }

    @Given("^(?:a|an) (Draft|Published|Archived|Evolved) Scale using this Indicator$")
    public void aScaleUsingThisIndicator(String scaleStatusName) {
        this.someScale = ScaleSteps.createScaleWithStatus(new ScaleId(UUID.fromString("someScale")), ScaleStatus.valueOf(scaleStatusName));
        this.someScale.attachIndicator(this.someIndicator.getId());
    }

    @Given("^another (Draft|Published|Archived|Evolved) Scale using this Indicator$")
    public void anotherScaleUsingThisIndicator(String scaleStatusName) {
        this.anotherScale = ScaleSteps.createScaleWithStatus(new ScaleId(UUID.fromString("someOtherScale")), ScaleStatus.valueOf(scaleStatusName));
        this.anotherScale.attachIndicator(this.someIndicator.getId());
    }

    @Given("^another (Draft|Published|Archived|Evolved) Indicator$")
    public void anotherDraftIndicatorNamed(String statusName) {
        this.anotherIndicator = createIndicatorWithStatus(new IndicatorId(UUID.fromString("anotherIndicator")), IndicatorStatus.valueOf(statusName));
    }

    @Given("^an Archived Indicator previously in (Draft|Published|Evolved) state$")
    public void anArchivedIndicatorPreviouslyState(String statusName) {
        this.someIndicator = createIndicatorWithStatus(new IndicatorId(UUID.fromString("someIndicator")), IndicatorStatus.valueOf(statusName));
        this.someIndicator.archive(null);
    }

    @When("^I evolve one into the other$")
    public void iEvolveOneIntoTheOther() {
        this.someIndicator.evolveInto(this.anotherIndicator.getId());
    }

    @Then("^the former Indicator status should be (Draft|Published|Archived|Evolved)$")
    public void indicatorStatusShouldBeEvolved(String statusName) {
        assertEquals(this.someIndicator.getStatus(), IndicatorStatus.valueOf(statusName));
    }

    @Then("^it should designate the latter as its evolved version$")
    public void itShouldDesignateTheLatterAsItsEvolvedVersion() {
        assertEquals(this.someIndicator.getEvolvedInto(), this.anotherIndicator.getId());
    }

}
