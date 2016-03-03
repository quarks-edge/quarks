package quarks.runtime.jsoncontrol;

/**
 * Control bean registered in {@link JsonControlService}.
 *
 * @param <T> Control interface.
 */
class ControlMBean<T> {

	private final Class<T> controlInterface;
	private final T control;
	
	ControlMBean(Class<T> controlInterface, T control) {
		
		this.controlInterface = controlInterface;
		this.control = control;
	}

	Class<T> getControlInterface() {
		return controlInterface;
	}

	T getControl() {
		return control;
	}
}
