package ranger.rest.v2

class HiveResource {
    PolicyResource url
    PolicyResource database
    PolicyResource udf
    PolicyResource column
    PolicyResource table

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        HiveResource that = (HiveResource) o

        if (column != that.column) return false
        if (database != that.database) return false
        if (table != that.table) return false
        if (udf != that.udf) return false
        if (url != that.url) return false

        return true
    }

    int hashCode() {
        int result
        result = (url != null ? url.hashCode() : 0)
        result = 31 * result + (database != null ? database.hashCode() : 0)
        result = 31 * result + (udf != null ? udf.hashCode() : 0)
        result = 31 * result + (column != null ? column.hashCode() : 0)
        result = 31 * result + (table != null ? table.hashCode() : 0)
        return result
    }
}
