using System;
using System.Collections.ObjectModel;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Text.RegularExpressions;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using Amqp;
using Amqp.Framing;
using Amqp.Types;

namespace AmqpGui
{
    public partial class MainWindow : Window
    { 

        private ReceiverLink _receiverLink;
        private Session _session;
        private Connection _connection;

        public ObservableCollection<string> Templates { get; } = new ObservableCollection<string>();

        private readonly string _execDir;
        private bool _listen = false;

        private bool Listen
        {
            get => _listen;
            set
            {
                _listen = value;
                ListenStatus.Content = value ? "Listening" : "Aus";
                ListenStatus.Foreground = value ? Brushes.Green : Brushes.Black;
                if (value)
                {
                    RbOn.IsEnabled = false;
                    RbOff.IsEnabled = true;
                    TbOutputQueue.IsEnabled = false;
                }
                else
                {
                    RbOn.IsEnabled = true;
                    RbOff.IsEnabled = false;
                    TbOutputQueue.IsEnabled = true;
                }
            }
        }

        public MainWindow()
        {
            InitializeComponent();
            _execDir = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
            Listen = false;
        }

        private void ButtonBase_OnClick(object senderObj, RoutedEventArgs e)
        {
            try
            {
                //amqp://vogler:vogler@localhost:5672";
                var address = $"amqp://{TbInputUser.Text}:{TbInputPwd.Text}@{TbInputHost.Text}:{TbInputPort.Text}"; 
                var connection = new Connection(new Address(address));
                var session = new Session(connection);
                var sender = new SenderLink(session, "test-sender", TbInputQueue.Text);

                var text = TbInputBody.Text;
                var improvedText = ApplyBodyTextFeatures(text);



                Console.WriteLine("Sending: "+improvedText);
                var bytes = Encoding.UTF8.GetBytes(improvedText);
                var message1 = new Message
                {
                    BodySection = new Data
                    {
                        Binary = bytes
                    },
                    Properties = new Amqp.Framing.Properties()
                    
                };

                var tmp = TbInputCorrelationId.Text;
                if (!string.IsNullOrEmpty(tmp))
                {
                    message1.Properties.CorrelationId = tmp;
                }

                tmp = TbInputReplyTo.Text;
                if (!string.IsNullOrEmpty(tmp))
                {
                    message1.Properties.ReplyTo = tmp;
                }

                tmp = TbInputSubject.Text;
                if (!string.IsNullOrEmpty(tmp))
                {
                    message1.Properties.Subject = tmp;
                }


                sender.Send(message1);
            }
            catch (Exception exception)
            {
                MessageBox.Show(exception.Message);
            }
        }

        private static string ApplyBodyTextFeatures(string text)
        {

            Guid.NewGuid();
            text = ReplaceWithFunc(text, "$guid:random()", () => Guid.NewGuid().ToString());
            text = ReplaceWithFunc(text, "$datetime:now()", () => DateTimeOffset.Now.ToString("yyyy-MM-dd'T'HH:mm:ss.FFFK"));
            text = ReplaceWithFunc(text, "$datetime:nowplus20()", () => DateTimeOffset.Now.AddMinutes(20).ToString("yyyy-MM-dd'T'HH:mm:ss.FFFK"));
            text = ReplaceWithFunc(text, "$datetime:nowplus2h()", () => DateTimeOffset.Now.AddHours(2).ToString("yyyy-MM-dd'T'HH:mm:ss.FFFK"));
            text = ReplaceWithFunc(text, "$datetime:nowplus5()", () => DateTimeOffset.Now.AddMinutes(5).ToString("yyyy-MM-dd'T'HH:mm:ss.FFFK"));
            text = ReplaceWithFunc(text, "$datetime:nowsubstract5()", () => DateTimeOffset.Now.Subtract(TimeSpan.FromMinutes(5)).ToString("yyyy-MM-dd'T'HH:mm:ss.FFFK"));
            text = ReplaceWithFunc(text, "$datetime:nowUTC()", () => DateTimeOffset.UtcNow.ToString("yyyy-MM-dd'T'HH:mm:ss.FFFK"));
            text = ReplaceWithFunc(text, "$float:random()", () =>
            {
                var min = 20.0;
                var max = 100.0;
                var randomNumber = new Random().NextDouble();

                randomNumber *= max - min;
                randomNumber += min;

                return randomNumber.ToString(CultureInfo.InvariantCulture);
            });

            //text = ReplaceWithFunc(text, "$float:random", () =>
            //{
            //    var min = 20.0;
            //    var max = 100.0;

            //    var pattern = @"\$float:random\((\d+) *, *(\d+)\)";
            //    var matches = Regex.Matches(text, pattern);

            //    if (matches.Count == 1 && matches[0].Groups.Count >= 3)
            //    {
            //        double.TryParse(matches[0].Groups[1].Value, out min);
            //        double.TryParse(matches[0].Groups[2].Value, out max);
            //    }

            //    var randomNumber = new Random().NextDouble();

            //    randomNumber *= max - min;
            //    randomNumber += min;

            //    return randomNumber.ToString(CultureInfo.InvariantCulture);
            //    });

            return text;
        }

        public static string ReplaceWithFunc(string text, string search, Func<string> func)
        {
            while (text.Contains(search))
            {
                text = ReplaceFirst(text, search, func.Invoke());
            }
            return text;
        }

        public static string ReplaceFirst(string text, string search, string replace)
        {
            var pos = text.IndexOf(search, StringComparison.Ordinal);
            if (pos < 0)
            {
                return text;
            }
            return text.Substring(0, pos) + replace + text.Substring(pos + search.Length);
        }

        private void ToggleButton_OnChecked(object sender, RoutedEventArgs e)
        {
            try
            {
                var button = sender as RadioButton;

                if (button.Name == "RbOn")
                {
                    //amqp://vogler:vogler@localhost:5672";
                    var address = $"amqp://{TbInputUser.Text}:{TbInputPwd.Text}@{TbInputHost.Text}:{TbInputPort.Text}"; 
                    var connection = new Connection(new Address(address));
                    _session = new Session(connection);
                    _receiverLink = new ReceiverLink(_session, "test-receiver", TbOutputQueue.Text);

                    _receiverLink.Start(1, OnMessage);
                }
                else if (button.Name == "RbOff")
                {
                    if(_receiverLink != null && !_receiverLink.IsClosed)
                    {
                        _receiverLink?.Close();
                    }

                    if (_session != null && !_session.IsClosed)
                    {
                        _session?.Close();
                    }
                }

            }
            catch (Exception exception)
            {
                MessageBox.Show(exception.Message);
            }
        }

        private void OnMessage(IReceiverLink receiver, Message message)
        {
            try
            {
                var bytes = message.Body as byte[];
                var text = Encoding.UTF8.GetString(bytes);

                Dispatcher.Invoke(() => {TbOutputBody.Text = text;});

                

                _receiverLink?.Accept(message);
            }
            catch (Exception exception)
            {
                MessageBox.Show(exception.Message);
            }
        }

        private void MainWindow_OnLoaded(object sender, RoutedEventArgs e)
        {
            LoadTemplates();

            CbInputTemplate.ItemsSource = Templates;
        }

        private void CbInputTemplate_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            var text = "";
            var file = CbInputTemplate.SelectedItem as string;
            if (file != null)
            {
                text = LoadTemplate(file);
            }
            TbInputBody.Text = text;
        }

        private void CbInputTemplate_OnMouseDown(object sender, MouseButtonEventArgs e)
        {
            
        }

        private void LoadTemplates()
        {
            try
            {
                var files = Directory.GetFiles(_execDir, "*.json");
                Templates.Clear();
                Templates.Add(" ");
                foreach (var s in files.ToList())
                {
                    var filename = Path.GetFileName(s);
                    Templates.Add(filename);
                }

                var dir2 = Path.Combine(_execDir, "templates");

                if (Directory.Exists(dir2))
                {
                    files = Directory.GetFiles(dir2, "*.json");
                    Templates.Clear();

                    foreach (var s in files.ToList())
                    {
                        var filename = Path.GetFileName(s);
                        Templates.Add(filename);
                    }
                }
            }
            catch (Exception)
            {
                // ignored
            }
        }

        private string LoadTemplate(string filename)
        {
            try
            {
                var combined = Path.Combine(_execDir, filename);

                if (!File.Exists(combined))
                {
                    combined = Path.Combine(_execDir, "templates", filename);

                    if (!File.Exists(combined))
                    {
                        return null;
                    }
                }

                return File.ReadAllText(combined);
            }
            catch (Exception)
            {
                return null;
            }
        }


        private void RbOn_OnClick(object sender, RoutedEventArgs e)
        {
            if (!Listen)
            {
                StartListening();
            }
        }
        private void RbOff_OnClick(object sender, RoutedEventArgs e)
        {
            StopListening();
        }

        private void StartListening()
        {
            try
            {
                var address = $"amqp://{TbInputUser.Text}:{TbInputPwd.Text}@{TbInputHost.Text}:{TbInputPort.Text}";
                _connection = new Connection(new Address(address));
                _session = new Session(_connection);

                var source = new Source()
                {
                    Address = TbOutputQueue.Text,
                    Capabilities = new Symbol[] {new Symbol("topic")}
                    //Register Queue as Multicast-Queue (topic)
                };

                _receiverLink = new ReceiverLink(_session, "test-receiver", source, null);

                _connection.Closed += OnClosed;
                _session.Closed += OnClosed;
                _receiverLink.Closed += OnClosed;

                _receiverLink.Start(1, OnMessage);
                Listen = true;
            }
            catch (Exception e)
            {
                MessageBox.Show($"{e.Message} - {e.StackTrace}");
            }
        }

        private void OnClosed(IAmqpObject sender, Error error)
        {
            _connection.Closed -= OnClosed;
            _session.Closed -= OnClosed;
            _receiverLink.Closed -= OnClosed;
            Listen = false;
            TbOutputBody.Text = error.ToString();
        }


        private void StopListening()
        {
            try
            {    
                if (_receiverLink != null && !_receiverLink.IsClosed)
                {
                    _receiverLink?.Close();
                }
                if (_session != null && !_session.IsClosed)
                {
                    _session?.Close();
                }
                if (_connection != null && !_connection.IsClosed)
                {
                    _connection?.Close();
                }
                Listen = false;
            }
            catch (Exception e)
            {
                MessageBox.Show($"{e.Message} - {e.StackTrace}");
            }
        }
    }
}
