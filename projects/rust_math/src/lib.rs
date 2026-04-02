/* #[no_mangle]: Makes sure the function is exported as square */

#[no_mangle]
pub extern "C" fn square(x: i32) -> i32 {
    x * x
}
