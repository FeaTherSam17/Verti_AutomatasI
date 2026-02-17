fn es_verdadero() -> bool {
    return true;
}

fn sumar(a: i32, b: i32) -> i32 {
    let mut resultado: i32 = a + b;
    return resultado;
}

fn main() {
    let mut x: i32 = 10;
    let y: i32 = 5;
    let texto: String = "Hola";
    let bandera: bool = true;

    print!("Inicio -> ", x, y, texto);
    println!("Suma inicial: ", sumar(x, y));

    x = x + y * 2;
    sumar(x, y);

    if es_verdadero() {
        println!("Entró al if", x);
    } else if bandera {
        println!("Entró al else if");
    } else {
        println!("Entró al else");
    }

    while bandera {
        println!("Ciclo while");
        return;
    }

    loop {
        println!("Ciclo loop");
        return;
    }
}