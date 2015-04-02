package com.swpbiz.foodcoma.models;

import com.swpbiz.foodcoma.RobolectricGradleTestRunner;
import com.swpbiz.foodcoma.activities.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static junit.framework.Assert.assertTrue;

/**
 * Created by vee on 4/1/15.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class InvitationTest {

    @Test
    public void testSomething() throws Exception {
        Invitation invitation = new Invitation();
        assertTrue(invitation.getTimeOfEvent() == 0);
    }

}
