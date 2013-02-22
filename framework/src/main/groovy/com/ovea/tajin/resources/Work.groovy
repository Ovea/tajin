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
