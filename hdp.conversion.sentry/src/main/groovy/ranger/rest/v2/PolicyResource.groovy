package ranger.rest.v2

class PolicyResource {
    def values = []
    boolean isExcludes = false
    boolean isRecursive = true

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        PolicyResource that = (PolicyResource) o

        if (isExcludes != that.isExcludes) return false
        if (isRecursive != that.isRecursive) return false
        if (values != that.values) return false

        return true
    }

    int hashCode() {
        int result
        result = (values != null ? values.hashCode() : 0)
        result = 31 * result + (isExcludes ? 1 : 0)
        result = 31 * result + (isRecursive ? 1 : 0)
        return result
    }
}
