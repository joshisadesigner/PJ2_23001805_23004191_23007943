/*
	Utilice esta clase para guardar la informacion de su
	AFN. NO DEBE CAMBIAR LOS NOMBRES DE LA CLASE NI DE LOS 
	METODOS que ya existen, sin embargo, usted es libre de 
	agregar los campos y metodos que desee.
*/
public class AFN{

	/*
		Implemente el constructor de la clase AFN
		que recibe como argumento un string que 
		representa el path del archivo que contiene
		la informacion del AFN (i.e. "Documentos/archivo.AFN").
		Puede utilizar la estructura de datos que desee
	*/
	public AFN(String path){
	}

	/*
		Implemente el metodo accept, que recibe como argumento
		un String que representa la cuerda a evaluar, y devuelve
		un boolean dependiendo de si la cuerda es aceptada o no 
		por el AFN. Recuerde lo aprendido en el proyecto 1.
	*/
	public boolean accept(String string){
		return false;
	}

	/*
		Implemente el metodo toAFD. Este metodo debe generar un archivo
		de texto que contenga los datos de un AFD segun las especificaciones
		del proyecto.
	*/
	public void toAFD(String afdPath){
	}

	/*
		El metodo main debe recibir como primer argumento el path
		donde se encuentra el archivo ".afd" y debe empezar a evaluar 
		cuerdas ingresadas por el usuario una a una hasta leer una cuerda vacia (""),
		en cuyo caso debe terminar. Tiene la libertad de implementar este metodo
		de la forma que desee. Si se envia la bandera "-to-afd", entonces en vez de
		evaluar, debe generar un archivo .afd
	*/
	public static void main(String[] args) throws Exception{
		
	}
}