fn es_verdadero() -> bool {
    return true;
}

fn sumar(a: i32, b: i32) -> i32 {
    let mut resultado: i32 = a + b;
    return resultado;
}

fn main() {
    /* ERROR 1: tipo incorrecto
    let mut x: i16 = 10; */
    let mut x: i16 = "10";
    /* ERROR 2: reasignación no mutable
    let mut y: i32 = 5;*/
    let y: i32 = 5;
    y = 8;
    /* ERROR 3: uso antes de inicializar
    z = 10*/
    let z: i32;
    print!("{}", z);

    let texto: String = "Hola";
    /* ERROR 4: redeclaración en mismo ámbito
    bandera = false;*/
    let bandera: bool = true;
    let bandera: bool = false;
    /* ERROR 5: variable no declarada en salida
    print!("Inicio -> ", x, y, "");*/
    print!("Inicio -> ", x, y, no_existe);
    /* ERROR 6: llamada con cantidad de argumentos incorrecta
    println!("Suma inicial: ", sumar(x, y));*/
    println!("Suma inicial: ", sumar(x));

    /* ERROR 7: tipo incompatible en asignación
    x = x + y * 2;*/
    x = "hola";
    /* ERROR 8: llamada a función no declarada
    sumar(x, y);*/
    multiplicar(x, y);

    let Name:String = "Milton";
    let Age:i8 = 21;
    /*ERROR 9: placeholders no coinciden
    print!("Name = {}, Age = {}", Name, Age);*/
    print!("Name = {}", Name, Age);

    /* ERROR 10: condición no booleana
    if es_verdadero() {*/
    if x {
        println!("Entró al if", x);
    } else if bandera {
        println!("Entró al else if");
    } else {
        println!("Entró al else");
    }

    /* ERROR 11: condición de while no booleana
    while bandera {*/
    while x {
        println!("Ciclo while");
        return;
    }

    loop {
        println!("Ciclo loop");
        return;
    }
}