/*
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
describe("tajin.timer", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('timer');
    });

    describe("tajin.timer.timers", function () {

        it("exposes all current running timers", function () {
            var t = tajin.timer.schedule('timer1', 500, false, $.noop);
            expect(t).toBe(tajin.timer.timers['timer1']);
        });

    });

    describe("tajin.timer.stop()", function () {

        it("stops a named timer (tajin.timer.stop())", function () {
            var t = tajin.timer.schedule('timer2', 1000, false, $.noop);
            expect(t.isActive()).toBe(true);
            tajin.timer.stop('timer2');
            expect(t.isActive()).toBe(false);
        });

    });

    describe("tajin.timer.schedule()", function () {

        it("can schedule a timer with no name (anonymous timer)", function () {
            var t = tajin.timer.schedule(null, 1000, false, $.noop);
            expect(t.id).toBeDefined();
            t.stop();
        });

        it("can schedule a timer with a name", function () {
            var t = tajin.timer.schedule('timer3', 1000, false, $.noop);
            expect(t.id).toBe('timer3');
        });

        it("can schedule a timer with a repeating interval", function () {
            var c = 0;
            var t = tajin.timer.schedule('timer4', 100, true, function () {
                c++;
                if (c === 4) {
                    t.stop();
                }
            });
            waitsFor(function () {
                return c === 4;
            }, 'too long', 600);
        });

        it("can override an existing timer by scheduling a new one with an existing name (previous timer is stopped)", function () {
            var c1 = 0,
                c2 = 0,
                t1 = tajin.timer.schedule('timer5', 1000, false, function () {
                    c1++;
                });
            expect(t1.isActive()).toBe(true);
            var t2 = tajin.timer.schedule('timer5', 100, false, function () {
                c2++;
            });
            expect(t1.isActive()).toBe(false);
            waitsFor(function () {
                return c2 === 1;
            }, 'too long', 600);
        });

    });

    describe("Timer instance t", function () {

        describe("t.toString()", function () {

            it("describes object", function () {
                var t = tajin.timer.schedule('timer6', 1000, false, $.noop);
                expect(t.toString).toBeDefined();
                t.stop();
            });

        });

        describe("t.stop()", function () {

            it("stops this timer", function () {
                var t = tajin.timer.schedule('timer7', 1000, false, $.noop);
                expect(t.isActive()).toBe(true);
                t.stop();
                expect(t.isActive()).toBe(false);
            });

        });

        describe("t.id", function () {

            it("returns timer ID", function () {
                var t = tajin.timer.schedule('timer8', 1000, false, $.noop);
                expect(t.id).toBe('timer8');
                t.stop();
            });

        });

        describe("t.isActive()", function () {

            it("returns if timer is till alive", function () {
                var t = tajin.timer.schedule('timer9', 1000, false, $.noop);
                expect(t.isActive()).toBe(true);
                t.stop();
                expect(t.isActive()).toBe(false);
            });

        });

    });

});
