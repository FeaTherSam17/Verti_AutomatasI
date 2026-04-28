fn base_calculo() -> i32 {
	let mut a: i32 = 1;
	let mut b: i32 = 2;
	let mut c: i32 = a + b;
	let mut d: i32 = c * 3;
	let mut e: i32 = d + a;
	let mut f: i32 = e - b;
	let mut g: i32 = f + 4;
	let mut h: i32 = g * 2;
	let mut i: i32 = h - 1;
	let mut j: i32 = i + 5;
	return j;
}

fn calcular_descuento(precio: i32) -> i32 {
	let mut tasa: i32 = 10;
	let mut mitad: i32 = tasa / 2;
	let mut descuento_base: i32 = precio * tasa;
	let mut descuento_extra: i32 = descuento_base + mitad;
	let mut ajuste: i32 = descuento_extra - tasa;
	let mut final_descuento: i32 = ajuste + 1;
	return final_descuento;
}

fn combinar_valores(x: i32, y: i32) -> i32 {
	let mut primero: i32 = x + y;
	let mut segundo: i32 = primero + x;
	let mut tercero: i32 = segundo + y;
	let mut cuarto: i32 = tercero * 2;
	let mut quinto: i32 = cuarto - primero;
	let mut sexto: i32 = quinto + segundo;
	return sexto;
}

fn main() {
	let mut inicio: i32 = 10;
	let mut incremento: i32 = 5;
	let mut multiplicador: i32 = 2;
	let mut base: i32 = inicio + incremento;
	let mut repetido: i32 = base + inicio;
	let mut mas_repetido: i32 = repetido * multiplicador;
	let mut ajuste1: i32 = mas_repetido + incremento;
	let mut ajuste2: i32 = ajuste1 - inicio;
	let mut ajuste3: i32 = ajuste2 + base;
	let mut ajuste4: i32 = ajuste3 * 3;
	let mut ajuste5: i32 = ajuste4 - 7;
	let mut ajuste6: i32 = ajuste5 + 1;
	let mut ajuste7: i32 = ajuste6 + inicio;
	let mut ajuste8: i32 = ajuste7 - incremento;
	let mut total: i32 = ajuste8 + multiplicador;
	let mut copia1: i32 = total;
	let mut copia2: i32 = copia1 + base;
	let mut copia3: i32 = copia2 + inicio;
	let mut copia4: i32 = copia3 + incremento;
	let mut copia5: i32 = copia4 * 2;
	let mut copia6: i32 = copia5 - total;
	let mut copia7: i32 = copia6 + 9;
	let mut copia8: i32 = copia7 - 4;
	let mut copia9: i32 = copia8 + copia1;
	let mut copia10: i32 = copia9 + copia2;

	let mut valor_base: i32 = base_calculo();
	let mut valor_descuento: i32 = calcular_descuento(20);
	let mut valor_combinado: i32 = combinar_valores(valor_base, valor_descuento);
	let mut valor_final: i32 = valor_combinado + inicio;
	let mut valor_final2: i32 = valor_final + incremento;
	let mut valor_final3: i32 = valor_final2 + multiplicador;
	let mut valor_final4: i32 = valor_final3 * 2;
	let mut valor_final5: i32 = valor_final4 - valor_base;
	let mut valor_final6: i32 = valor_final5 + valor_descuento;
	let mut valor_final7: i32 = valor_final6 + 100;
	let mut valor_final8: i32 = valor_final7 - 25;
	let mut valor_final9: i32 = valor_final8 + 3;
	let mut valor_final10: i32 = valor_final9 + copia10;

	let texto: String = "Inicio del programa no optimizado";
	let titulo: String = "Verti AutomatasI";
	let etiqueta: String = "Prueba de propagacion de constantes";
	let bandera: bool = true;

	print!("{}", texto);
	println!("{}", titulo);
	println!("{}", etiqueta);
	println!("Valor base: ", valor_base);
	println!("Valor descuento: ", valor_descuento);
	println!("Valor combinado: ", valor_combinado);
	println!("Valor final 1: ", valor_final);
	println!("Valor final 2: ", valor_final2);
	println!("Valor final 3: ", valor_final3);
	println!("Valor final 4: ", valor_final4);
	println!("Valor final 5: ", valor_final5);
	println!("Valor final 6: ", valor_final6);
	println!("Valor final 7: ", valor_final7);
	println!("Valor final 8: ", valor_final8);
	println!("Valor final 9: ", valor_final9);
	println!("Valor final 10: ", valor_final10);
	println!("Copia 1: ", copia1);
	println!("Copia 2: ", copia2);
	println!("Copia 3: ", copia3);
	println!("Copia 4: ", copia4);
	println!("Copia 5: ", copia5);
	println!("Copia 6: ", copia6);
	println!("Copia 7: ", copia7);
	println!("Copia 8: ", copia8);
	println!("Copia 9: ", copia9);
	println!("Copia 10: ", copia10);

	if bandera {
		let mut rama1: i32 = valor_final10 + 1;
		let mut rama2: i32 = rama1 + 2;
		let mut rama3: i32 = rama2 + 3;
		let mut rama4: i32 = rama3 * 2;
		println!("Rama if: ", rama4);
	} else if false {
		let mut rama5: i32 = valor_final10 - 1;
		let mut rama6: i32 = rama5 - 2;
		println!("Rama else if: ", rama6);
	} else {
		println!("Rama else");
	}

	while bandera {
		let mut ciclo1: i32 = 1;
		let mut ciclo2: i32 = ciclo1 + 1;
		let mut ciclo3: i32 = ciclo2 + 1;
		let mut ciclo4: i32 = ciclo3 + 1;
		println!("Ciclo while: ", ciclo4);
		return;
	}

	loop {
		let mut bucle1: i32 = 3;
		let mut bucle2: i32 = bucle1 + 4;
		let mut bucle3: i32 = bucle2 * 2;
		let mut bucle4: i32 = bucle3 - 5;
		println!("Ciclo loop: ", bucle4);
		return;
	}
}
