import javax.script.ScriptEngineManager

def manager = new ScriptEngineManager()
def engine = manager.getEngineByName("JavaScript")

engine.put('tajin', [
    merge: { println it }
])

engine.eval('tajin.merge({"toto.js":[""]})')

