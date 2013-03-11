import javax.script.ScriptEngineManager

def manager = new ScriptEngineManager()
def engine = manager.getEngineByName("JavaScript")

class TajinFacade {
    void merge(def data) {
        println data.a
        println data.a[0]
        println data.a[1]
        println data.a[3]
    }
}

engine.put('tajin', new TajinFacade())

engine.eval('tajin.merge({a:[1, 2, 3, true]})')

