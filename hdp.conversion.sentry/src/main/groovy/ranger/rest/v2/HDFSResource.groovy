package ranger.rest.v2
/*
"path": {
    "values": [
            "/*"
    ],
    "isExcludes": false,
    "isRecursive": true
}
*/

class HDFSResource {
    PolicyResource path;

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        HDFSResource that = (HDFSResource) o

        if (path != that.path) return false

        return true
    }

    int hashCode() {
        return (path != null ? path.hashCode() : 0)
    }
}
