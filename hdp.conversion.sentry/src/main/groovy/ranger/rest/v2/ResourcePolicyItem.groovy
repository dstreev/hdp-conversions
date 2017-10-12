package ranger.rest.v2

class ResourcePolicyItem {
    def accesses = []
    def users = []
    def groups = []
    def conditions = []
    boolean delegateAdmin = false
}
