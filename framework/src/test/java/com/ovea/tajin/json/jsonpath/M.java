package com.ovea.tajin.json.jsonpath;

import com.ovea.tajin.json.JSON;
import com.ovea.tajin.json.JSONObject;
import com.ovea.tajin.json.JSONType;
import org.hamcrest.*;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.AllOf.allOf;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class M {

    public static org.hamcrest.Matcher<JSONType> hasEntry(String key, Object value) {
        return new IsMapContaining(Matchers.equalTo(key), Matchers.equalTo(JSON.valueOf(value)));
    }

    public static <T> org.hamcrest.Matcher<JSONType> equalTo(T operand) {
        return org.hamcrest.core.IsEqual.equalTo(JSON.valueOf(operand));
    }

    public static <T> Matcher<JSONType> hasItem(T element) {
        return new IsCollectionContaining(equalTo(JSON.valueOf(element)));
    }

    public static <T> Matcher<JSONType> hasItems(T... elements) {
        List<Matcher<? super JSONType>> all = new ArrayList<>(elements.length);
        for (T element : elements) {
            all.add(new IsCollectionContaining(equalTo(JSON.valueOf(element))));
        }
        return allOf(all);
    }

    private static class IsCollectionContaining extends TypeSafeDiagnosingMatcher<JSONType> {
        private final Matcher<? super JSONType> elementMatcher;

        public IsCollectionContaining(Matcher<? super JSONType> elementMatcher) {
            this.elementMatcher = elementMatcher;
        }

        @Override
        protected boolean matchesSafely(JSONType collection, Description mismatchDescription) {
            boolean isPastFirst = false;
            for (JSONType item : collection.asArray()) {
                if (elementMatcher.matches(item)) {
                    return true;
                }
                if (isPastFirst) {
                    mismatchDescription.appendText(", ");
                }
                elementMatcher.describeMismatch(item, mismatchDescription);
                isPastFirst = true;
            }
            return false;
        }

        public void describeTo(Description description) {
            description
                .appendText("a collection containing ")
                .appendDescriptionOf(elementMatcher);
        }

    }

    private static class IsMapContaining extends TypeSafeMatcher<JSONType> {
        private final Matcher<String> keyMatcher;
        private final Matcher<JSONType> valueMatcher;

        public IsMapContaining(Matcher<String> keyMatcher, Matcher<JSONType> valueMatcher) {
            this.keyMatcher = keyMatcher;
            this.valueMatcher = valueMatcher;
        }

        @Override
        public boolean matchesSafely(JSONType map) {
            JSONObject obj = map.asObject();
            for (String key : obj.keys()) {
                if (keyMatcher.matches(key) && valueMatcher.matches(obj.get(key))) {
                    return true;
                }
            }
            return false;
        }

        public void describeTo(Description description) {
            description.appendText("map containing [")
                .appendDescriptionOf(keyMatcher)
                .appendText("->")
                .appendDescriptionOf(valueMatcher)
                .appendText("]");
        }
    }
}
