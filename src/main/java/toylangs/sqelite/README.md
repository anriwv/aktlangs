# Listitöötluse keel Sqelite

SQL-laadne andmetöötluskeel lihtsate päringute tegemiseks operatsioone listide peal. Sqelite võimaldab määrata andmeallika, filtreerida andmeid tõeväärtusavaldise põhjal ning teisendada tulemust märgitud struktuuri. 


## AST
Keele AST klassid paiknevad `toylangs.sqelite.ast` paketis ja nende ülemklassiks on `SqeliteNode`:

* `SqeliteNum` – täisarvuliteraal;
* `SqeliteVar` – iteratsioonimuutuja (enamasti `x`);
* `SqeliteBinOp` – binaarsed operaatorid (aritmeetika: `+`, `-`, `*`, `/` ja võrdlused: `>`, `<`, `==`, `!=`, `>=`, `<=`);
* `SqeliteAssign` – muutuja omistamine;
* `SqeliteProg` – terviklik programm, mis koosneb omistamistest ja peaavaldisest;
* `SqeliteSource` – andmeallikas (täisarvude list, nt `from [1, 2, 3]`);
* `SqeliteQuery` – päringu struktuur, mis rakendab allikale `where` ja `select` teisendusi;
* `SqeliteWhere` – tingimuslik filtreerimine;
* `SqeliteSelect` – elementide teisendus (projektsioon/map);
* `SqeliteMin` – vähima elemendi leidmine (`min()`);
* `SqeliteMax` – suurima elemendi leidmine (`max()`);
* `SqeliteAvg` – elementide keskmise leidmine (`avg()`).

```sqelite
from [1, 2, 3, 4] where x > 2 select x + 10
```

Klassis `SqeliteNode` on staatilised abimeetodid, millega saab mugavamalt abstraktseid süntaksipuid luua.

## Alusosa: SqeliteEvaluator
Klassis `SqeliteEvaluator` tuleb implementeerida evalueerija, mis väärtustab päringu ja tagastab tulemuse (kas täisarvu või uue listi):

1. **Numbrid ja literaalid:** Arvulised konstandid evalueeruvad iseeneseks.
2. **Operaatorid:** Toetatud on aritmeetilised ja võrdlusoperaatorid.
3. **Muutujad:** Defineerimata muutujate puhul peab programm andma veateate. Defineeritud muutujad loetakse keskkonnast.
4. **Prioriteedid ja assotsiatiivsus:** Avaldiste väärtustamisel peavad kehtima matemaatilised prioriteedid.
5. **Andmeallikad:** `from [...]` initsialiseerib andmeallika, tagastades listi.
6. **Päringud:** `where` filtreerib välja elemendid, mille korral tingimusavaldis ei kehti (astmeline käivitamine). `select` rakendab igale läbivale elemendile teisendusavaldise ja koostab neist tulemuslisti.
7. **Aggregatsioonid:** `min`, `max` ja `avg` arvutavad listi elementide pealt vastava väärtuse. Tühja listi puhul visatakse erind.
8. **Keerukad päringud:** Avaldiste kombineerimine, kus päringu tulemus (list) on omakorda sisendiks uuele päringule või aggregatsioonile.

## Põhiosa: SqeliteAst
Failis `Sqelite.g4` tuleb implementeerida grammatika ja klassis `SqeliteAst` tuleb implementeerida meetod `parseTreeToAst`, mis teisendab parsepuu AST-iks. Süntaksile kehtivad järgmised nõuded:

1. `from [1, 2]` parsib komadega eraldatud täisarve andmeallikaks. Sisendiks sobib ka tühi massiiv `[]`.
2. Numbrid on tavalised täisarvud ning muutujad koosnevad ladina tähtedest.
3. Loogilised ja aritmeetilised tehted on vasakassotsiatiivsed. Prioriteedid on samad nagu matemaatikas (kõige kõrgem on korrutamine/jagamine, siis liitmine/lahutamine ja lõpuks võrdlused).
4. Sulgude abil saab operaatorite prioriteete muuta.
5. Tühisümboleid (tühikud, reavahetused jne) tuleb ignoreerida.
6. Omistamine on tähistatud `=` märgiga ning neid võib komadega eraldades programmi algusesse kirjutada.

## Lõviosa: SqeliteCompiler
Klassis `SqeliteCompiler` tuleb implementeerida meetod, mis kompileerib päringu CMa programmiks. Kompileerimisele kehtivad järgmised nõuded:

1. Kompileeritud kood opereerib CMa pinul (stackil). 
2. Kuna tegemist on listitöötluskeelega, kasutab kompilaator mälu (heap), kuhu paigutatakse elemente ning hoitakse meeles listi pikkust.
3. Üle iteratsiooni minnes laetakse element indeksipõhiselt mälust ning hakatakse seda tingimuste abil modifitseerima. See tähendab `JUMPZ` kasutamist, et jätta elemente vahele (`where` korral).
4. Pärast listi töötlemist tehakse andmetest uus list või leitakse aggregatsioon ja tulemus paigutatakse pinu tippu.
5. Vigade puhul (näiteks tühjast listist keskmise leidmine või puuduv muutuja) kompileeritakse `HALT` instruktsioon.
