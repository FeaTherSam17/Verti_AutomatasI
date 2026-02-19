fn es_verdadero() -> bool {
    return true;
}

fn sumar(a: i32, b: i32) -> i32 {
    let mut resultado: i32 = a + b;
    return resultado;
}

fn main() {
    let mut x: i16 = 10;
    let y: i32 = 5;
    let texto: String = "Hola";
    let bandera: bool = true;

    print!("Inicio -> ", x, y, "");
    println!("Suma inicial: ", sumar(x, y));

    x = x + y * 2;
    sumar(x, y);

    let Name:String = "Milton el Insano";
    let Age:i8 = 21;
    print!("Name = {}, Age = {}", Name, Age);

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