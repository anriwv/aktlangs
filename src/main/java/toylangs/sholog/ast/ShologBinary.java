package toylangs.sholog.ast;

public sealed interface ShologBinary extends ShologNode permits ShologEager, ShologLazy {
}
