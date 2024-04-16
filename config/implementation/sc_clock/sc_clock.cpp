#include <systemc.h>

class sc_clockx : sc_module, sc_interface
{
  public:
    sc_event edge;
    sc_event change;

    bool val;
    bool _val;
            
    int delta;

	private:
		const int period;

		SC_HAS_PROCESS(sc_clockx);

		void run(void) {
			edge.notify(SC_ZERO_TIME);
			change.notify(SC_ZERO_TIME);
			while(true) {
				wait(period/2, SC_NS);
				edge.notify(SC_ZERO_TIME);
				change.notify(SC_ZERO_TIME);
				val = !val;
			}
		}

	public:
		sc_clockx(sc_module_name name, int periodparam): sc_module(name)
	{
		SC_THREAD(run);
		period = periodparam;
		val = true;
		_val = true;
	}

  bool read() {
      return val;
    }
    
  void write(bool newval) {
      _val = newval;
      request_update();
    }
    
    void update() {
      if (!(_val == val))
      {
	  change.notify(SC_ZERO_TIME);
	  delta = sc_delta_count();
      }
      val = _val; 
    }
            
};
