# Listitöötlust toetav SQLite 

SQL-laadne andmetöötluskeel lihtsate päringute tegemiseks listide peal. Keel võimaldab määrata andmeallika, filtreerida elemente tõeväärtusavaldise põhjal ning teisendada tulemust märgitud struktuuri.

## AST

* _SqliteQuery_ – terve päring, mis seob andmeallika, filtreerimise ja teisenduse;
* _SqliteSource_ – andmeallikas;
* _SqliteWhere_ – filtreerimistingimus elementide väljajätmiseks;
* _SqliteSelect_ – elementide projektsioon ja teisendus;
* _SqliteVar_ – iteraatori muutuja;
* _SqliteNum_ – täisarvuliteraal;
* _SqliteBinOp_ – binaarsed aritmeetilised (+, -, *, /) ja võrdlevad/loogilised (>, <, ==, !=) operaatorid
```sqlite
from [1, 2, 3, 4]
where x > 2
select x + 10
```
tulemuseks on list [13, 14].

