/**
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
package com.ovea.tajin.resources

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-21
 */
class Work {

    static Work COMPLETED = new Work(null)

    final ResourceBuilder builder
    final Status status
    final def data

    Work(ResourceBuilder builder, Status status = Status.COMPLETED, data = null) {
        this.builder = builder
        this.status = status
        this.data = data
    }

    boolean isIncomplete() { status == Status.INCOMPLETE }

    Work complete() { builder.complete(this) }

    @Override
    public String toString() {
        return "${builder?.class?.simpleName ?: 'Work'}:${data ?: 'done'}"
    }

    static Work incomplete(ResourceBuilder builder, def data = null) { new Work(builder, Status.INCOMPLETE, data) }

    static enum Status {
        COMPLETED,
        INCOMPLETE
    }
}
